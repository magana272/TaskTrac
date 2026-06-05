package task.trak.app.server.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.CreateTaskRequest;
import task.trak.model.dto.request.UpdateTaskRequest;
import task.trak.model.exception.TrakException;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskRoutes {

    public static class TaskListHandler implements HttpHandler {
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
                TaskService taskService = ServiceFactory.taskService();
                Map<String, String> query = JsonHelper.parseQuery(exchange.getRequestURI().getQuery());
                String assignee = query.get("assignee");

                List<TaskDTO> tasks;
                if (assignee != null && !assignee.isEmpty()) {
                    tasks = taskService.listByAssignee(assignee);
                } else {
                    tasks = taskService.listAll();
                }
                JsonHelper.sendJson(exchange, 200, tasks);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                CreateTaskRequest request = JsonHelper.fromJson(body, CreateTaskRequest.class);
                request.validate();

                TaskService taskService = ServiceFactory.taskService();
                TaskDTO task = taskService.create(request);
                JsonHelper.sendJson(exchange, 201, task);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class TaskDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String idStr = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/tasks/");
            if (idStr.isEmpty()) {
                JsonHelper.sendError(exchange, 400, "Task ID is required in path");
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "Invalid task ID");
                return;
            }

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> handleGet(exchange, id);
                case "PUT" -> handleUpdate(exchange, id);
                case "DELETE" -> handleDelete(exchange, id);
                default -> JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleGet(HttpExchange exchange, Long id) throws IOException {
            try {
                TaskService taskService = ServiceFactory.taskService();
                TaskDTO task = taskService.getById(id);
                if (task == null) {
                    JsonHelper.sendError(exchange, 404, "Task not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, task);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleUpdate(HttpExchange exchange, Long id) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                UpdateTaskRequest bodyRequest = JsonHelper.fromJson(body, UpdateTaskRequest.class);
                UpdateTaskRequest request = new UpdateTaskRequest(
                        id,
                        bodyRequest.title(),
                        bodyRequest.status(),
                        bodyRequest.assignedTo(),
                        bodyRequest.summary(),
                        bodyRequest.estimate(),
                        bodyRequest.completionNote()
                );
                request.validate();

                TaskService taskService = ServiceFactory.taskService();
                TaskDTO task = taskService.updateById(request);
                if (task == null) {
                    JsonHelper.sendError(exchange, 404, "Task not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, task);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleDelete(HttpExchange exchange, Long id) throws IOException {
            try {
                TaskService taskService = ServiceFactory.taskService();
                boolean deleted = taskService.deleteById(id);
                if (!deleted) {
                    JsonHelper.sendError(exchange, 404, "Task not found");
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
