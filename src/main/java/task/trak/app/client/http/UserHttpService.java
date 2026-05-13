package task.trak.app.client.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import task.trak.api.dto.UserDTO;
import task.trak.api.service.UserService;

public class UserHttpService implements UserService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public UserDTO create(String username, String firstName, String lastName, String email, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("firstName", firstName);
        body.addProperty("lastName", lastName);
        body.addProperty("email", email);
        body.addProperty("password", password);
        String response = ApiClient.post("/api/users", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, UserDTO.class);
    }

    @Override
    public UserDTO getByUsername(String username) {
        String response = ApiClient.get("/api/users/" + username);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, UserDTO.class);
    }

    @Override
    public UserDTO getByEmail(String email) {
        // Server doesn't have a by-email endpoint.
        // This method is only used in signup validation which goes through the server.
        return null;
    }

    @Override
    public boolean deleteByUsername(String username) {
        String response = ApiClient.delete("/api/users/" + username);
        if (response == null) return false;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("deleted") && json.get("deleted").getAsBoolean();
    }

    @Override
    public UserDTO updateByUsername(String username, String newFirstName, String newLastName, String newEmail, String newPassword) {
        JsonObject body = new JsonObject();
        if (newFirstName != null) body.addProperty("firstName", newFirstName);
        if (newLastName != null) body.addProperty("lastName", newLastName);
        if (newEmail != null) body.addProperty("email", newEmail);
        if (newPassword != null) body.addProperty("password", newPassword);
        String response = ApiClient.put("/api/users/" + username, body.toString());
        if (response == null) return null;
        return gson.fromJson(response, UserDTO.class);
    }

    @Override
    public boolean authenticate(String username, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        try {
            String response = ApiClient.post("/api/auth/login", body.toString());
            if (response == null) return false;
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            return !json.has("error");
        } catch (RuntimeException e) {
            return false;
        }
    }
}
