package task.trak.app.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static String baseUrl = "http://localhost:8080";
    private static String authToken = null;

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String get(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET();
        addHeaders(builder);
        return send(builder.build());
    }

    public static String post(String path, String jsonBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        addHeaders(builder);
        builder.header("Content-Type", "application/json");
        return send(builder.build());
    }

    public static String put(String path, String jsonBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        addHeaders(builder);
        builder.header("Content-Type", "application/json");
        return send(builder.build());
    }

    public static String delete(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE();
        addHeaders(builder);
        return send(builder.build());
    }

    private static void addHeaders(HttpRequest.Builder builder) {
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
    }

    private static String send(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status == 404) {
                return null;
            }
            if (status >= 400) {
                // Try to extract error message from JSON {"error": "..."}
                String errorMsg = body;
                try {
                    var json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                    if (json.has("error")) {
                        errorMsg = json.get("error").getAsString();
                    }
                } catch (Exception ignored) {
                }
                throw new RuntimeException(errorMsg);
            }
            return body;
        } catch (RuntimeException e) {
            throw e;
        } catch (java.net.ConnectException e) {
            throw new RuntimeException("Cannot connect to server at " + baseUrl, e);
        } catch (Exception e) {
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }
}
