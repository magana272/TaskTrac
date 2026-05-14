package task.trak.app.client.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import task.trak.api.dto.UserDTO;
import task.trak.api.dto.request.CreateUserRequest;
import task.trak.api.dto.request.UpdateUserRequest;
import task.trak.api.service.UserService;

public class UserHttpService implements UserService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public UserDTO create(CreateUserRequest request) {
        JsonObject body = new JsonObject();
        body.addProperty("username", request.username());
        body.addProperty("firstName", request.firstName());
        body.addProperty("lastName", request.lastName());
        body.addProperty("email", request.email());
        body.addProperty("password", request.password());
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
    public UserDTO updateByUsername(UpdateUserRequest request) {
        JsonObject body = new JsonObject();
        if (request.firstName() != null) body.addProperty("firstName", request.firstName());
        if (request.lastName() != null) body.addProperty("lastName", request.lastName());
        if (request.email() != null) body.addProperty("email", request.email());
        if (request.password() != null) body.addProperty("password", request.password());
        String response = ApiClient.put("/api/users/" + request.username(), body.toString());
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
