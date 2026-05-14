package task.trak.app.server.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.trak.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthRoutes {

    public static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String body = JsonHelper.readBody(exchange);
                Map<String, String> req = JsonHelper.fromJson(body, Map.class);
                String username = req.get("username");
                String password = req.get("password");

                if (username == null || password == null) {
                    JsonHelper.sendError(exchange, 400, "username and password are required");
                    return;
                }

                UserService userService = ServiceFactory.userService();
                boolean valid = userService.authenticate(username, password);
                if (!valid) {
                    JsonHelper.sendError(exchange, 401, "Invalid username or password");
                    return;
                }

                String token = SessionManager.createToken(username);
                Map<String, String> resp = new LinkedHashMap<>();
                resp.put("token", token);
                resp.put("username", username);
                JsonHelper.sendJson(exchange, 200, resp);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String body = JsonHelper.readBody(exchange);
                Map<String, String> req = JsonHelper.fromJson(body, Map.class);
                String username = req.get("username");
                String password = req.get("password");
                String firstName = req.get("firstName");
                String lastName = req.get("lastName");
                String email = req.get("email");

                if (username == null || password == null || firstName == null || lastName == null || email == null) {
                    JsonHelper.sendError(exchange, 400, "username, password, firstName, lastName, and email are required");
                    return;
                }

                AuthService authService = ServiceFactory.authService();
                Session session = authService.signup(firstName, lastName, username, email, password);
                if (session == null) {
                    JsonHelper.sendError(exchange, 400, "Signup failed");
                    return;
                }

                String token = SessionManager.createToken(username);
                Map<String, String> resp = new LinkedHashMap<>();
                resp.put("token", token);
                resp.put("username", username);
                JsonHelper.sendJson(exchange, 200, resp);
            } catch (IllegalArgumentException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String token = SessionManager.extractToken(exchange);
                SessionManager.removeToken(token);
                Map<String, String> resp = new LinkedHashMap<>();
                resp.put("message", "Logged out");
                JsonHelper.sendJson(exchange, 200, resp);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }
}
