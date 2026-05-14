package task.trak.app.client.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.CreateTaskRequest;
import task.trak.model.dto.request.UpdateTaskRequest;
import task.trak.api.service.TaskService;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

public class TaskHttpService implements TaskService {
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public TaskDTO create(CreateTaskRequest request) {
        JsonObject body = new JsonObject();
        body.addProperty("title", request.title());
        if (request.projectName() != null) body.addProperty("projectName", request.projectName());
        if (request.assignedTo() != null) body.addProperty("assignedTo", request.assignedTo());
        if (request.summary() != null) body.addProperty("summary", request.summary());
        if (request.deadline() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            body.addProperty("deadline", sdf.format(request.deadline()));
        }
        if (request.estimate() != null) body.addProperty("estimate", request.estimate());
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
    public TaskDTO updateById(UpdateTaskRequest request) {
        JsonObject body = new JsonObject();
        if (request.title() != null) body.addProperty("title", request.title());
        if (request.status() != null) body.addProperty("status", request.status());
        if (request.assignedTo() != null) body.addProperty("assignedTo", request.assignedTo());
        if (request.summary() != null) body.addProperty("summary", request.summary());
        if (request.estimate() != null) body.addProperty("estimate", request.estimate());
        String response = ApiClient.put("/api/tasks/" + request.id(), body.toString());
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
