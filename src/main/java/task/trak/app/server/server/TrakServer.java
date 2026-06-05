package task.trak.app.server.server;

import com.sun.net.httpserver.HttpServer;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.config.WorkspaceConfig;
import task.trak.app.server.dao.DAOFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class TrakServer {
    private final HttpServer server;
    private final int port;

    public TrakServer(int port) throws IOException {
        this.port = port;

        // Init store (same as TTApp.init())
        if (!Files.exists(Path.of(TTApp.storedir)) || !Files.isDirectory(Path.of(TTApp.storedir))) {
            Files.createDirectories(Path.of(TTApp.storedir));
        }
        WorkspaceConfig config = WorkspaceConfig.load();
        String fmt = config.getStore_format();
        if ("json".equalsIgnoreCase(fmt)) {
            DAOFactory.setFormat(DAOFactory.Format.JSON);
        } else if ("mongo".equalsIgnoreCase(fmt)) {
            DAOFactory.setFormat(DAOFactory.Format.MONGO);
        } else if ("duckdb".equalsIgnoreCase(fmt)) {
            DAOFactory.setFormat(DAOFactory.Format.DUCKDB);
        } else if ("redis".equalsIgnoreCase(fmt)) {
            DAOFactory.setFormat(DAOFactory.Format.REDIS);
        } else {
            DAOFactory.setFormat(DAOFactory.Format.PARQUET);
        }

        // Create HttpServer
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth routes (no auth required — login/signup)
        server.createContext("/api/auth/login", new AuthRoutes.LoginHandler());
        server.createContext("/api/auth/signup", new AuthRoutes.SignupHandler());
        server.createContext("/api/auth/logout", AuthFilter.requireAuth(new AuthRoutes.LogoutHandler()));

        // User routes (POST /api/users is open for registration; detail requires auth)
        server.createContext("/api/users", new UserRoutes.UserListHandler());
        server.createContext("/api/users/", AuthFilter.requireAuth(new UserRoutes.UserDetailHandler()));

        // Project routes (all require auth)
        server.createContext("/api/projects", AuthFilter.requireAuth(new ProjectRoutes.ProjectListHandler()));
        server.createContext("/api/projects/id/", AuthFilter.requireAuth(new ProjectRoutes.ProjectByIdHandler()));
        server.createContext("/api/projects/name/", AuthFilter.requireAuth(new ProjectRoutes.ProjectByNameHandler()));
        server.createContext("/api/projects/", AuthFilter.requireAuth(new ProjectRoutes.ProjectDetailHandler()));

        // Task routes (all require auth)
        server.createContext("/api/tasks", AuthFilter.requireAuth(new TaskRoutes.TaskListHandler()));
        server.createContext("/api/tasks/", AuthFilter.requireAuth(new TaskRoutes.TaskDetailHandler()));

        // Sprint routes (all require auth)
        server.createContext("/api/sprints", AuthFilter.requireAuth(new SprintRoutes.SprintListHandler()));
        server.createContext("/api/sprints/name/", AuthFilter.requireAuth(new SprintRoutes.SprintByNameHandler()));
        server.createContext("/api/sprints/", AuthFilter.requireAuth(new SprintRoutes.SprintDetailHandler()));

        // Backlog routes (all require auth)
        server.createContext("/api/backlogs", AuthFilter.requireAuth(new BacklogRoutes.BacklogListHandler()));
        server.createContext("/api/backlogs/", AuthFilter.requireAuth(new BacklogRoutes.BacklogDetailHandler()));

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[0] + ". Using default 8080.");
            }
        }
        TrakServer server = new TrakServer(port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public void start() {
        server.start();
        System.out.println("Trak server started on port " + getPort());
    }

    public void stop() {
        server.stop(0);
        System.out.println("Trak server stopped.");
    }
}
