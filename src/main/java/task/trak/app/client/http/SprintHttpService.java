package task.trak.app.client.http;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.request.CreateSprintRequest;
import task.trak.model.dto.request.UpdateSprintRequest;
import task.trak.api.service.SprintService;

import java.lang.reflect.Type;
import java.util.List;

public class SprintHttpService implements SprintService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public SprintDTO create(CreateSprintRequest request) {
        JsonObject body = new JsonObject();
        body.addProperty("name", request.name());
        body.addProperty("projectName", request.projectName());
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
    public SprintDTO update(UpdateSprintRequest request) {
        SprintDTO sprint;
        if (request.projectName() != null) {
            sprint = getByNameAndProject(request.name(), request.projectName());
            if (sprint == null) throw new RuntimeException("Sprint not found: " + request.name() + " in project " + request.projectName());
        } else {
            sprint = getByName(request.name());
            if (sprint == null) throw new RuntimeException("Sprint not found: " + request.name());
        }
        JsonObject body = new JsonObject();
        if (request.startDate() != null) body.addProperty("startDate", request.startDate());
        if (request.endDate() != null) body.addProperty("endDate", request.endDate());
        if (request.taskIds() != null) {
            JsonArray arr = new JsonArray();
            request.taskIds().forEach(arr::add);
            body.add("taskIds", arr);
        }
        if (request.completed() != null) {
            body.addProperty("completed", request.completed());
        }
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
