package task.trak.app.server.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.model.dto.request.UpdateUserRequest;
import task.trak.model.exception.TrakException;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserRoutes {

    public static class UserListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("POST".equals(method)) {
                handleCreate(exchange);
            } else {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                CreateUserRequest request = JsonHelper.fromJson(body, CreateUserRequest.class);
                request.validate();

                UserService userService = ServiceFactory.userService();
                UserDTO user = userService.create(request);
                JsonHelper.sendJson(exchange, 201, user);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class UserDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String username = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/users/");
            if (username.isEmpty()) {
                JsonHelper.sendError(exchange, 400, "Username is required in path");
                return;
            }

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> handleGet(exchange, username);
                case "PUT" -> handleUpdate(exchange, username);
                case "DELETE" -> handleDelete(exchange, username);
                default -> JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleGet(HttpExchange exchange, String username) throws IOException {
            try {
                UserService userService = ServiceFactory.userService();
                UserDTO user = userService.getByUsername(username);
                if (user == null) {
                    JsonHelper.sendError(exchange, 404, "User not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, user);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleUpdate(HttpExchange exchange, String username) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                UpdateUserRequest bodyRequest = JsonHelper.fromJson(body, UpdateUserRequest.class);
                UpdateUserRequest request = new UpdateUserRequest(
                        username,
                        bodyRequest.firstName(),
                        bodyRequest.lastName(),
                        bodyRequest.email(),
                        bodyRequest.password()
                );
                request.validate();

                UserService userService = ServiceFactory.userService();
                UserDTO user = userService.updateByUsername(request);
                if (user == null) {
                    JsonHelper.sendError(exchange, 404, "User not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, user);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleDelete(HttpExchange exchange, String username) throws IOException {
            try {
                UserService userService = ServiceFactory.userService();
                boolean deleted = userService.deleteByUsername(username);
                if (!deleted) {
                    JsonHelper.sendError(exchange, 404, "User not found");
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
