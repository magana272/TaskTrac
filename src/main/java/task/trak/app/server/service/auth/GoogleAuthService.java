package task.trak.app.server.service.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.api.service.UserService;
import task.trak.app.server.service.user.TrakUserService;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

/**
 * Verifies Google ID tokens and creates/retrieves users.
 */
public class GoogleAuthService {

    private final UserService userService;
    private final String clientId;

    public GoogleAuthService() {
        this(new TrakUserService());
    }

    public GoogleAuthService(UserService userService) {
        this.userService = userService;
        this.clientId = loadClientId();
    }

    /**
     * Verifies a Google ID token and returns the username of the matched or newly created user.
     */
    public String verifyAndLogin(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("ID token is required.");
        }

        JsonObject payload = verifyToken(idToken);

        String aud = payload.has("aud") ? payload.get("aud").getAsString() : null;
        if (!clientId.equals(aud)) {
            throw new SecurityException("Token audience mismatch.");
        }

        String email = payload.has("email") ? payload.get("email").getAsString() : null;
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google token does not contain an email.");
        }

        String givenName = payload.has("given_name") ? payload.get("given_name").getAsString() : "";
        String familyName = payload.has("family_name") ? payload.get("family_name").getAsString() : "";

        // Look up existing user by email
        UserDTO existing = userService.getByEmail(email);
        if (existing != null) {
            return existing.userName();
        }

        // Create new OAuth-only user (no password)
        String username = generateUsername(email);
        userService.create(new CreateUserRequest(username, givenName, familyName, email, null));
        return username;
    }

    private JsonObject verifyToken(String idToken) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SecurityException("Invalid Google token.");
            }

            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a username from the email local part, ensuring it meets the 5-17 char alphanumeric requirement.
     */
    private String generateUsername(String email) {
        String local = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        if (local.length() < 5) {
            local = local + "user1";
        }
        if (local.length() > 17) {
            local = local.substring(0, 17);
        }

        // Ensure uniqueness
        String candidate = local;
        int suffix = 1;
        while (userService.getByUsername(candidate) != null) {
            String base = local.length() > 14 ? local.substring(0, 14) : local;
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private static String loadClientId() {
        // Try System env first
        String envVal = System.getenv("TRAK_GOOGLE_CLIENT_ID");
        if (envVal != null && !envVal.isBlank()) return envVal;

        // Fall back to .env file
        return loadFromEnvFile("clientID");
    }

    public static String loadClientSecret() {
        String envVal = System.getenv("TRAK_GOOGLE_CLIENT_SECRET");
        if (envVal != null && !envVal.isBlank()) return envVal;

        return loadFromEnvFile("clientsecret");
    }

    public static String loadFromEnvFile(String key) {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            throw new IllegalStateException("Google OAuth not configured: .env file not found.");
        }
        try {
            Properties props = new Properties();
            props.load(Files.newBufferedReader(envPath));
            String value = props.getProperty(key);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Google OAuth not configured: " + key + " not set.");
            }
            return value;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file: " + e.getMessage(), e);
        }
    }
}