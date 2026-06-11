package task.trak.app.client.gui.view.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import task.trak.app.server.service.auth.GoogleAuthService;

import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Desktop loopback OAuth flow for Google Sign-In.
 * Opens the browser for consent, captures the auth code via a local callback server,
 * exchanges it for tokens, and returns the id_token.
 */
public class GoogleOAuthHelper {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    /**
     * Runs the full OAuth flow and returns the Google id_token.
     * Blocks until the user completes the flow or times out (2 minutes).
     */
    public static String getIdToken() throws Exception {
        String clientId = GoogleAuthService.loadFromEnvFile("clientID");
        String clientSecret = GoogleAuthService.loadClientSecret();

        CompletableFuture<String> codeFuture = new CompletableFuture<>();

        // Start local callback server on a free port
        HttpServer callbackServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        int port = callbackServer.getAddress().getPort();
        String redirectUri = "http://localhost:" + port + "/oauth/callback";

        callbackServer.createContext("/oauth/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        code = param.substring(5);
                        break;
                    }
                }
            }

            String html;
            if (code != null) {
                html = "<html><body><h2>Sign-in successful!</h2><p>You can close this window.</p></body></html>";
                codeFuture.complete(code);
            } else {
                String error = "unknown";
                if (query != null) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("error=")) {
                            error = param.substring(6);
                            break;
                        }
                    }
                }
                html = "<html><body><h2>Sign-in failed</h2><p>" + error + "</p></body></html>";
                codeFuture.completeExceptionally(new RuntimeException("OAuth error: " + error));
            }

            byte[] resp = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        callbackServer.setExecutor(null);
        callbackServer.start();

        try {
            // Open browser to Google consent page
            String authUrl = AUTH_URL
                    + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                    + "&access_type=offline";

            Desktop.getDesktop().browse(URI.create(authUrl));

            // Wait for the auth code (2 minute timeout)
            String authCode = codeFuture.get(2, TimeUnit.MINUTES);

            // Exchange auth code for tokens
            return exchangeCodeForIdToken(authCode, clientId, clientSecret, redirectUri);
        } finally {
            callbackServer.stop(1);
        }
    }

    private static String exchangeCodeForIdToken(String code, String clientId, String clientSecret, String redirectUri) throws Exception {
        String formBody = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Token exchange failed: " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("id_token")) {
            throw new RuntimeException("No id_token in token response.");
        }
        return json.get("id_token").getAsString();
    }
}
