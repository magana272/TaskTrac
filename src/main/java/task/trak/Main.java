package task.trak;

import task.trak.api.service.ServiceFactory;
import task.trak.app.client.ApiClient;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.config.WorkspaceConfig;
import task.trak.app.client.gui.controller.TTAppGUI;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.SessionDAO;
import task.trak.app.server.server.TrakServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--server".equals(args[0])) {
            // Server mode: start REST API
            ServiceFactory.registerLocalServices();
            int port = 8080;
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            try {
                TrakServer server = new TrakServer(port);
                server.start();
            } catch (Exception e) {
                System.err.println("Failed to start server: " + e.getMessage());
            }
        } else if (args.length > 0 && "--gui".equals(args[0])) {
            // GUI mode
            boolean seedTest = Arrays.asList(args).contains("--test");
            boolean local = Arrays.asList(args).contains("--local");
            if (!local) {
                String url = parseServerUrl(args);
                ApiClient.setBaseUrl(url);
                ServiceFactory.registerHttpServices();
            }
            if (local) {
                initLocalStore();
                ServiceFactory.registerLocalServices();
            }
            TTAppGUI gui = new TTAppGUI(seedTest, local);
            if (local) {
                gui.setSessionPersistence(SessionDAO::load, SessionDAO::save);
            }
            gui.initStore(local);
        } else {
            // CLI mode — default to LOCAL unless --remote
            boolean remote = Arrays.asList(args).contains("--remote");
            if (remote) {
                String url = parseServerUrl(args);
                ApiClient.setBaseUrl(url);
                ServiceFactory.registerHttpServices();
                args = Arrays.stream(args)
                        .filter(a -> !a.equals("--remote") && !a.startsWith("--server-url"))
                        .toArray(String[]::new);
            }

            // Initialize local store for non-remote mode
            if (!remote) {
                initLocalStore();
                ServiceFactory.registerLocalServices();
                ensureGuestAccount();
            }

            TTApp app;
            if (!remote) {
                app = TTApp.createLocal(args, SessionDAO::load, SessionDAO::save);
            } else {
                app = new TTApp(args);
            }
            app.save();
        }
    }

    private static void initLocalStore() {
        if (!Files.exists(Path.of(TTApp.storedir)) || !Files.isDirectory(Path.of(TTApp.storedir))) {
            try {
                Files.createDirectories(Path.of(TTApp.storedir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    }

    private static void ensureGuestAccount() {
        var userService = ServiceFactory.userService();
        if (userService.getByUsername("guest") == null) {
            userService.create("guest", "Guest", "Admin", "guest@trak", "guest");
        }
    }

    private static String parseServerUrl(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--server-url".equals(args[i]) && i + 1 < args.length) {
                return args[i + 1];
            }
        }
        return "http://localhost:8080";
    }
}
