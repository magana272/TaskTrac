package task.trak.app.server.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.request.CreateSprintRequest;
import task.trak.model.dto.request.UpdateSprintRequest;
import task.trak.model.exception.TrakException;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SprintRoutes {

    public static class SprintListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> handleList(exchange);
                case "POST" -> handleCreate(exchange);
                default -> JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleList(HttpExchange exchange) throws IOException {
            try {
                SprintService sprintService = ServiceFactory.sprintService();
                List<SprintDTO> sprints = sprintService.listAll();
                JsonHelper.sendJson(exchange, 200, sprints);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                CreateSprintRequest request = JsonHelper.fromJson(body, CreateSprintRequest.class);
                request.validate();

                SprintService sprintService = ServiceFactory.sprintService();
                SprintDTO sprint = sprintService.create(request);
                JsonHelper.sendJson(exchange, 201, sprint);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class SprintByNameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String name = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/sprints/name/");
                Map<String, String> query = JsonHelper.parseQuery(exchange.getRequestURI().getQuery());
                String project = query.get("project");

                SprintService sprintService = ServiceFactory.sprintService();
                SprintDTO sprint;
                if (project != null && !project.isEmpty()) {
                    sprint = sprintService.getByNameAndProject(name, project);
                } else {
                    sprint = sprintService.getByName(name);
                }

                if (sprint == null) {
                    JsonHelper.sendError(exchange, 404, "Sprint not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, sprint);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class SprintDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String idStr = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/sprints/");
            if (idStr.isEmpty()) {
                JsonHelper.sendError(exchange, 400, "Sprint ID is required in path");
                return;
            }

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> handleGet(exchange, idStr);
                case "PUT" -> handleUpdate(exchange, idStr);
                case "DELETE" -> handleDelete(exchange, idStr);
                default -> JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleGet(HttpExchange exchange, String idStr) throws IOException {
            try {
                Long id = Long.parseLong(idStr);
                SprintService sprintService = ServiceFactory.sprintService();
                SprintDTO sprint = sprintService.getById(id);
                if (sprint == null) {
                    JsonHelper.sendError(exchange, 404, "Sprint not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, sprint);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "Invalid sprint ID");
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleUpdate(HttpExchange exchange, String idStr) throws IOException {
            try {
                Long id = Long.parseLong(idStr);
                SprintService sprintService = ServiceFactory.sprintService();

                SprintDTO existing = sprintService.getById(id);
                if (existing == null) {
                    JsonHelper.sendError(exchange, 404, "Sprint not found");
                    return;
                }

                String body = JsonHelper.readBody(exchange);
                UpdateSprintRequest bodyRequest = JsonHelper.fromJson(body, UpdateSprintRequest.class);

                // If addTask is in the body, merge it into existing task IDs
                List<Long> taskIds = bodyRequest.taskIds();
                if (taskIds == null) {
                    // Check for legacy addTask field
                    Map<String, Object> raw = JsonHelper.fromJson(body,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());
                    Number addTaskNum = (Number) raw.get("addTask");
                    if (addTaskNum != null) {
                        Long taskId = addTaskNum.longValue();
                        taskIds = new ArrayList<>(existing.taskIds() != null ? existing.taskIds() : List.of());
                        if (!taskIds.contains(taskId)) {
                            taskIds.add(taskId);
                        }
                    }
                }

                UpdateSprintRequest request = new UpdateSprintRequest(
                        existing.name(),
                        existing.projectName(),
                        bodyRequest.startDate(),
                        bodyRequest.endDate(),
                        taskIds
                );
                request.validate();

                SprintDTO sprint = sprintService.update(request);
                JsonHelper.sendJson(exchange, 200, sprint);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "Invalid sprint ID");
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleDelete(HttpExchange exchange, String idStr) throws IOException {
            try {
                // DELETE uses name, but we receive an ID-like path segment
                // The spec says DELETE /api/sprints/{name}, so treat the param as a name
                SprintService sprintService = ServiceFactory.sprintService();
                boolean deleted = sprintService.deleteByName(idStr);
                if (!deleted) {
                    JsonHelper.sendError(exchange, 404, "Sprint not found");
                    return;
                }
                Map<String, Boolean> resp = new LinkedHashMap<>();
                resp.put("deleted", true);
                JsonHelper.sendJson(exchange, 200, resp);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }
}
