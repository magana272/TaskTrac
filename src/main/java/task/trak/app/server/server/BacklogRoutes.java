package task.trak.app.server.server;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.trak.model.dto.BacklogDTO;
import task.trak.model.dto.request.CreateBacklogRequest;
import task.trak.model.exception.TrakException;
import task.trak.api.service.BacklogService;
import task.trak.api.service.ServiceFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BacklogRoutes {

    public static class BacklogListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String body = JsonHelper.readBody(exchange);
                CreateBacklogRequest request = JsonHelper.fromJson(body, CreateBacklogRequest.class);
                request.validate();

                BacklogService backlogService = ServiceFactory.backlogService();
                BacklogDTO backlog = backlogService.create(request);
                JsonHelper.sendJson(exchange, 201, backlog);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class BacklogDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String name = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/backlogs/");
            if (name.isEmpty()) {
                JsonHelper.sendError(exchange, 400, "Backlog name is required in path");
                return;
            }

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> handleGet(exchange, name);
                case "PUT" -> handleUpdate(exchange, name);
                case "DELETE" -> handleDelete(exchange, name);
                default -> JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleGet(HttpExchange exchange, String name) throws IOException {
            try {
                BacklogService backlogService = ServiceFactory.backlogService();
                BacklogDTO backlog = backlogService.getByName(name);
                if (backlog == null) {
                    JsonHelper.sendError(exchange, 404, "Backlog not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, backlog);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleUpdate(HttpExchange exchange, String name) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                Map<String, Object> req = JsonHelper.fromJson(body,
                        new TypeToken<Map<String, Object>>() {
                        }.getType());
                Number addTaskNum = (Number) req.get("addTask");
                Number removeTaskNum = (Number) req.get("removeTask");

                BacklogService backlogService = ServiceFactory.backlogService();
                BacklogDTO backlog = null;

                if (addTaskNum != null) {
                    backlog = backlogService.addTask(name, addTaskNum.longValue());
                } else if (removeTaskNum != null) {
                    backlog = backlogService.removeTask(name, removeTaskNum.longValue());
                } else {
                    JsonHelper.sendError(exchange, 400, "addTask or removeTask is required");
                    return;
                }

                if (backlog == null) {
                    JsonHelper.sendError(exchange, 404, "Backlog not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, backlog);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleDelete(HttpExchange exchange, String name) throws IOException {
            try {
                BacklogService backlogService = ServiceFactory.backlogService();
                boolean deleted = backlogService.deleteByName(name);
                if (!deleted) {
                    JsonHelper.sendError(exchange, 404, "Backlog not found");
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
