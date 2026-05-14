package task.trak.app.client.cli;

import task.trak.model.dto.request.CreateUserRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.http.ApiClient;
import task.trak.app.client.config.WorkspaceConfig;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.SessionDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class CLIMain {
    public static void main(String[] args) {
        boolean remote = Arrays.asList(args).contains("--remote");
        if (remote) {
            String url = parseServerUrl(args);
            ApiClient.setBaseUrl(url);
            ServiceFactory.registerHttpServices();
            args = Arrays.stream(args)
                    .filter(a -> !a.equals("--remote") && !a.startsWith("--server-url"))
                    .toArray(String[]::new);
            TTApp app = new TTApp(args);
            app.save();
        } else {
            initLocalStore();
            ServiceFactory.registerLocalServices();
            ensureGuestAccount();
            TTApp app = TTApp.createLocal(args, SessionDAO::load, SessionDAO::save);
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
            userService.create(new CreateUserRequest("guest", "Guest", "Admin", "guest@trak", "guest"));
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
