package task.trak.app.client.http;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import task.trak.api.dto.SprintDTO;
import task.trak.api.service.SprintService;

import java.lang.reflect.Type;
import java.util.List;

public class SprintHttpService implements SprintService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public SprintDTO create(String name, String projectName) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("projectName", projectName);
        String response = ApiClient.post("/api/sprints", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public SprintDTO getById(Long id) {
        String response = ApiClient.get("/api/sprints/" + id);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public SprintDTO getByName(String name) {
        String response = ApiClient.get("/api/sprints/name/" + name);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public SprintDTO getByNameAndProject(String name, String projectName) {
        String response = ApiClient.get("/api/sprints/name/" + name + "?project=" + projectName);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public boolean deleteByName(String name) {
        String response = ApiClient.delete("/api/sprints/" + name);
        if (response == null) return false;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("deleted") && json.get("deleted").getAsBoolean();
    }

    @Override
    public SprintDTO updateByName(String name, String newStartDate, String newEndDate) {
        SprintDTO sprint = getByName(name);
        if (sprint == null) throw new RuntimeException("Sprint not found: " + name);
        JsonObject body = new JsonObject();
        if (newStartDate != null) body.addProperty("startDate", newStartDate);
        if (newEndDate != null) body.addProperty("endDate", newEndDate);
        String response = ApiClient.put("/api/sprints/" + sprint.id(), body.toString());
        if (response == null) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public SprintDTO updateByNameAndProject(String name, String projectName, String newStartDate, String newEndDate) {
        SprintDTO sprint = getByNameAndProject(name, projectName);
        if (sprint == null) throw new RuntimeException("Sprint not found: " + name + " in project " + projectName);
        JsonObject body = new JsonObject();
        if (newStartDate != null) body.addProperty("startDate", newStartDate);
        if (newEndDate != null) body.addProperty("endDate", newEndDate);
        String response = ApiClient.put("/api/sprints/" + sprint.id(), body.toString());
        if (response == null) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public SprintDTO updateTaskIds(String name, List<Long> taskIds) {
        SprintDTO sprint = getByName(name);
        if (sprint == null) throw new RuntimeException("Sprint not found: " + name);
        JsonObject body = new JsonObject();
        JsonArray arr = new JsonArray();
        taskIds.forEach(arr::add);
        body.add("taskIds", arr);
        String response = ApiClient.put("/api/sprints/" + sprint.id(), body.toString());
        if (response == null) return null;
        return gson.fromJson(response, SprintDTO.class);
    }

    @Override
    public List<SprintDTO> listAll() {
        String response = ApiClient.get("/api/sprints");
        if (response == null) return List.of();
        Type listType = new TypeToken<List<SprintDTO>>() {
        }.getType();
        return gson.fromJson(response, listType);
    }
}
