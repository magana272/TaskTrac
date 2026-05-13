package task.trak.app.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import task.trak.api.dto.BacklogDTO;
import task.trak.api.service.BacklogService;

public class BacklogHttpService implements BacklogService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public BacklogDTO create(String name, String projectName) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("projectName", projectName);
        String response = ApiClient.post("/api/backlogs", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, BacklogDTO.class);
    }

    @Override
    public BacklogDTO getByName(String name) {
        String response = ApiClient.get("/api/backlogs/" + name);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, BacklogDTO.class);
    }

    @Override
    public boolean deleteByName(String name) {
        String response = ApiClient.delete("/api/backlogs/" + name);
        if (response == null) return false;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("deleted") && json.get("deleted").getAsBoolean();
    }

    @Override
    public BacklogDTO addTask(String backlogName, Long taskId) {
        JsonObject body = new JsonObject();
        body.addProperty("addTask", taskId);
        String response = ApiClient.put("/api/backlogs/" + backlogName, body.toString());
        if (response == null) return null;
        return gson.fromJson(response, BacklogDTO.class);
    }

    @Override
    public BacklogDTO removeTask(String backlogName, Long taskId) {
        JsonObject body = new JsonObject();
        body.addProperty("removeTask", taskId);
        String response = ApiClient.put("/api/backlogs/" + backlogName, body.toString());
        if (response == null) return null;
        return gson.fromJson(response, BacklogDTO.class);
    }
}
