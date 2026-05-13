package task.trak.app.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import task.trak.api.dto.ProjectDTO;
import task.trak.api.service.ProjectService;

import java.lang.reflect.Type;
import java.util.List;

public class ProjectHttpService implements ProjectService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public ProjectDTO create(String name) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        String response = ApiClient.post("/api/projects", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, ProjectDTO.class);
    }

    @Override
    public ProjectDTO create(String name, String summary, String ownerUsername, List<String> memberUsernames) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        if (summary != null) body.addProperty("summary", summary);
        if (ownerUsername != null) body.addProperty("ownerUsername", ownerUsername);
        if (memberUsernames != null) {
            JsonArray members = new JsonArray();
            memberUsernames.forEach(members::add);
            body.add("memberUsernames", members);
        }
        String response = ApiClient.post("/api/projects", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, ProjectDTO.class);
    }

    @Override
    public ProjectDTO getById(Long id) {
        String response = ApiClient.get("/api/projects/id/" + id);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, ProjectDTO.class);
    }

    @Override
    public ProjectDTO getByName(String name) {
        String response = ApiClient.get("/api/projects/name/" + name);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, ProjectDTO.class);
    }

    @Override
    public boolean deleteByName(String name) {
        String response = ApiClient.delete("/api/projects/" + name);
        if (response == null) return false;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("deleted") && json.get("deleted").getAsBoolean();
    }

    @Override
    public ProjectDTO updateByName(String projectName, String newName, String newSummary, List<String> newMemberUsernames) {
        JsonObject body = new JsonObject();
        if (newName != null) body.addProperty("name", newName);
        if (newSummary != null) body.addProperty("summary", newSummary);
        if (newMemberUsernames != null) {
            JsonArray members = new JsonArray();
            newMemberUsernames.forEach(members::add);
            body.add("memberUsernames", members);
        }
        String response = ApiClient.put("/api/projects/" + projectName, body.toString());
        if (response == null) return null;
        return gson.fromJson(response, ProjectDTO.class);
    }

    @Override
    public List<ProjectDTO> listAll() {
        String response = ApiClient.get("/api/projects");
        if (response == null) return List.of();
        Type listType = new TypeToken<List<ProjectDTO>>() {
        }.getType();
        return gson.fromJson(response, listType);
    }

    @Override
    public List<ProjectDTO> listByUser(String username) {
        String response = ApiClient.get("/api/projects?user=" + username);
        if (response == null) return List.of();
        Type listType = new TypeToken<List<ProjectDTO>>() {
        }.getType();
        return gson.fromJson(response, listType);
    }

    @Override
    public ProjectDTO addMember(String projectName, String username) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        String response = ApiClient.post("/api/projects/" + projectName + "/members", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, ProjectDTO.class);
    }
}
