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
        } else {
            DAOFactory.setFormat(DAOFactory.Format.PARQUET);
        }

        // Create HttpServer
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register auth routes
        server.createContext("/api/auth/login", new AuthRoutes.LoginHandler());
        server.createContext("/api/auth/signup", new AuthRoutes.SignupHandler());
        server.createContext("/api/auth/logout", new AuthRoutes.LogoutHandler());

        // Register user routes
        server.createContext("/api/users", new UserRoutes.UserListHandler());
        server.createContext("/api/users/", new UserRoutes.UserDetailHandler());

        // Register project routes
        server.createContext("/api/projects", new ProjectRoutes.ProjectListHandler());
        server.createContext("/api/projects/id/", new ProjectRoutes.ProjectByIdHandler());
        server.createContext("/api/projects/name/", new ProjectRoutes.ProjectByNameHandler());
        server.createContext("/api/projects/", new ProjectRoutes.ProjectDetailHandler());

        // Register task routes
        server.createContext("/api/tasks", new TaskRoutes.TaskListHandler());
        server.createContext("/api/tasks/", new TaskRoutes.TaskDetailHandler());

        // Register sprint routes
        server.createContext("/api/sprints", new SprintRoutes.SprintListHandler());
        server.createContext("/api/sprints/name/", new SprintRoutes.SprintByNameHandler());
        server.createContext("/api/sprints/", new SprintRoutes.SprintDetailHandler());

        // Register backlog routes
        server.createContext("/api/backlogs", new BacklogRoutes.BacklogListHandler());
        server.createContext("/api/backlogs/", new BacklogRoutes.BacklogDetailHandler());

        server.setExecutor(null);
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

    public void start() {
        server.start();
        System.out.println("Trak server started on port " + port);
    }

    public void stop() {
        server.stop(0);
        System.out.println("Trak server stopped.");
    }
}
