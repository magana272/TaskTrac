package task.trak.app.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import task.trak.api.dto.TaskDTO;
import task.trak.api.service.TaskService;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TaskHttpService implements TaskService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public TaskDTO create(String title, String projectName, String assignedTo, String summary, Date deadline, String estimate) {
        JsonObject body = new JsonObject();
        body.addProperty("title", title);
        if (projectName != null) body.addProperty("projectName", projectName);
        if (assignedTo != null) body.addProperty("assignedTo", assignedTo);
        if (summary != null) body.addProperty("summary", summary);
        if (deadline != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            body.addProperty("deadline", sdf.format(deadline));
        }
        if (estimate != null) body.addProperty("estimate", estimate);
        String response = ApiClient.post("/api/tasks", body.toString());
        if (response == null) return null;
        return gson.fromJson(response, TaskDTO.class);
    }

    @Override
    public TaskDTO getById(Long id) {
        String response = ApiClient.get("/api/tasks/" + id);
        if (response == null) return null;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")) return null;
        return gson.fromJson(response, TaskDTO.class);
    }

    @Override
    public boolean deleteById(Long id) {
        String response = ApiClient.delete("/api/tasks/" + id);
        if (response == null) return false;
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("deleted") && json.get("deleted").getAsBoolean();
    }

    @Override
    public TaskDTO updateById(Long id, String newTitle, String newStatus, String newAssignedTo, String newSummary) {
        JsonObject body = new JsonObject();
        if (newTitle != null) body.addProperty("title", newTitle);
        if (newStatus != null) body.addProperty("status", newStatus);
        if (newAssignedTo != null) body.addProperty("assignedTo", newAssignedTo);
        if (newSummary != null) body.addProperty("summary", newSummary);
        String response = ApiClient.put("/api/tasks/" + id, body.toString());
        if (response == null) return null;
        return gson.fromJson(response, TaskDTO.class);
    }

    @Override
    public List<TaskDTO> listAll() {
        String response = ApiClient.get("/api/tasks");
        if (response == null) return List.of();
        Type listType = new TypeToken<List<TaskDTO>>() {
        }.getType();
        return gson.fromJson(response, listType);
    }

    @Override
    public List<TaskDTO> listByAssignee(String username) {
        String response = ApiClient.get("/api/tasks?assignee=" + username);
        if (response == null) return List.of();
        Type listType = new TypeToken<List<TaskDTO>>() {
        }.getType();
        return gson.fromJson(response, listType);
    }
}
