package task.trak.app.server.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.request.CreateProjectRequest;
import task.trak.api.dto.request.UpdateProjectRequest;
import task.trak.api.exception.TrakException;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectRoutes {

    public static class ProjectListHandler implements HttpHandler {
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
                ProjectService projectService = ServiceFactory.projectService();
                Map<String, String> query = JsonHelper.parseQuery(exchange.getRequestURI().getQuery());
                String user = query.get("user");

                List<ProjectDTO> projects;
                if (user != null && !user.isEmpty()) {
                    projects = projectService.listByUser(user);
                } else {
                    projects = projectService.listAll();
                }
                JsonHelper.sendJson(exchange, 200, projects);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                CreateProjectRequest request = JsonHelper.fromJson(body, CreateProjectRequest.class);
                request.validate();

                ProjectService projectService = ServiceFactory.projectService();
                ProjectDTO project = projectService.create(request);
                JsonHelper.sendJson(exchange, 201, project);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class ProjectByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String idStr = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/projects/id/");
                Long id = Long.parseLong(idStr);
                ProjectService projectService = ServiceFactory.projectService();
                ProjectDTO project = projectService.getById(id);
                if (project == null) {
                    JsonHelper.sendError(exchange, 404, "Project not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, project);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "Invalid project ID");
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class ProjectByNameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                JsonHelper.sendError(exchange, 405, "Method not allowed");
                return;
            }
            try {
                String name = JsonHelper.extractPathParam(exchange.getRequestURI().getPath(), "/api/projects/name/");
                ProjectService projectService = ServiceFactory.projectService();
                ProjectDTO project = projectService.getByName(name);
                if (project == null) {
                    JsonHelper.sendError(exchange, 404, "Project not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, project);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }

    public static class ProjectDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Check if it's a members sub-route: /api/projects/{name}/members
            if (path.endsWith("/members")) {
                if (!"POST".equals(method)) {
                    JsonHelper.sendError(exchange, 405, "Method not allowed");
                    return;
                }
                String nameSegment = path.substring("/api/projects/".length(),
                        path.length() - "/members".length());
                handleAddMember(exchange, nameSegment);
                return;
            }

            String name = JsonHelper.extractPathParam(path, "/api/projects/");
            if (name.isEmpty()) {
                JsonHelper.sendError(exchange, 400, "Project name is required in path");
                return;
            }

            switch (method) {
                case "PUT" -> handleUpdate(exchange, name);
                case "DELETE" -> handleDelete(exchange, name);
                default -> JsonHelper.sendError(exchange, 405, "Method not allowed");
            }
        }

        private void handleUpdate(HttpExchange exchange, String name) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                UpdateProjectRequest bodyRequest = JsonHelper.fromJson(body, UpdateProjectRequest.class);
                UpdateProjectRequest request = new UpdateProjectRequest(
                        name,
                        bodyRequest.newName(),
                        bodyRequest.newSummary(),
                        bodyRequest.newMemberUsernames()
                );
                request.validate();

                ProjectService projectService = ServiceFactory.projectService();
                ProjectDTO project = projectService.updateByName(request);
                if (project == null) {
                    JsonHelper.sendError(exchange, 404, "Project not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, project);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleDelete(HttpExchange exchange, String name) throws IOException {
            try {
                ProjectService projectService = ServiceFactory.projectService();
                boolean deleted = projectService.deleteByName(name);
                if (!deleted) {
                    JsonHelper.sendError(exchange, 404, "Project not found");
                    return;
                }
                Map<String, Boolean> resp = new LinkedHashMap<>();
                resp.put("deleted", true);
                JsonHelper.sendJson(exchange, 200, resp);
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }

        private void handleAddMember(HttpExchange exchange, String projectName) throws IOException {
            try {
                String body = JsonHelper.readBody(exchange);
                Map<String, String> req = JsonHelper.fromJson(body, Map.class);
                String username = req.get("username");

                if (username == null || username.isEmpty()) {
                    JsonHelper.sendError(exchange, 400, "username is required");
                    return;
                }

                ProjectService projectService = ServiceFactory.projectService();
                ProjectDTO project = projectService.addMember(projectName, username);
                if (project == null) {
                    JsonHelper.sendError(exchange, 404, "Project not found");
                    return;
                }
                JsonHelper.sendJson(exchange, 200, project);
            } catch (TrakException e) {
                JsonHelper.sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                JsonHelper.sendError(exchange, 500, e.getMessage());
            }
        }
    }
}
