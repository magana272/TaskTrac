package task.trak.app.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import task.trak.api.model.Session;
import task.trak.api.service.AuthService;

public class AuthHttpService implements AuthService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public Session login(String username, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        String response = ApiClient.post("/api/auth/login", body.toString());
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) {
            throw new RuntimeException(json.get("error").getAsString());
        }
        String token = json.get("token").getAsString();
        String user = json.get("username").getAsString();
        ApiClient.setAuthToken(token);
        return new Session(user);
    }

    @Override
    public Session signup(String firstName, String lastName, String username, String email, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("firstName", firstName);
        body.addProperty("lastName", lastName);
        body.addProperty("username", username);
        body.addProperty("email", email);
        body.addProperty("password", password);
        String response = ApiClient.post("/api/auth/signup", body.toString());
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) {
            throw new RuntimeException(json.get("error").getAsString());
        }
        String token = json.get("token").getAsString();
        String user = json.get("username").getAsString();
        ApiClient.setAuthToken(token);
        return new Session(user);
    }

    @Override
    public void logout() {
        try {
            ApiClient.post("/api/auth/logout", "{}");
        } catch (RuntimeException ignored) {
            // Best effort; clear token regardless
        }
        ApiClient.setAuthToken(null);
    }

    @Override
    public Session getCurrentSession() {
        // Client manages session locally; no server-side session retrieval
        return null;
    }

    @Override
    public boolean isLoggedIn() {
        return ApiClient.getAuthToken() != null;
    }
}
