package task.trak.app.client.gui;

import task.trak.app.client.http.ApiClient;
import task.trak.app.client.http.TaskHttpService;
import task.trak.app.client.http.ProjectHttpService;
import task.trak.app.client.http.SprintHttpService;
import task.trak.app.client.http.UserHttpService;
import task.trak.app.client.http.BacklogHttpService;
import task.trak.app.client.http.AuthHttpService;
import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.controller.TaskController;
import task.trak.app.client.gui.controller.ProjectController;
import task.trak.app.client.gui.controller.SprintController;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.viewmodel.TaskViewModel;
import task.trak.app.client.gui.viewmodel.ProjectViewModel;
import task.trak.app.client.gui.viewmodel.SprintViewModel;
import task.trak.app.client.gui.viewmodel.UserViewModel;
import task.trak.app.client.gui.view.MainFrame;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.SessionDAO;
import task.trak.app.server.server.TrakServer;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class GUIMain {
    public static void main(String[] args) {
        boolean seedTest = Arrays.asList(args).contains("--test");
        boolean local = Arrays.asList(args).contains("--local");
        if (!local) {
            String url = parseServerUrl(args);
            ApiClient.setBaseUrl(url);
        } else {
            if (!Files.exists(Path.of(TTApp.storedir)) || !Files.isDirectory(Path.of(TTApp.storedir))) {
                try {
                    Files.createDirectories(Path.of(TTApp.storedir));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create store directory: " + e.getMessage(), e);
                }
            }
            try {
                task.trak.api.service.ServiceFactory.registerLocalServices();
                TrakServer embeddedServer = new TrakServer(0);
                embeddedServer.start();
                ApiClient.setBaseUrl("http://localhost:" + embeddedServer.getPort());
                Runtime.getRuntime().addShutdownHook(new Thread(embeddedServer::stop));
            } catch (IOException e) {
                throw new RuntimeException("Failed to start embedded server: " + e.getMessage(), e);
            }
        }

        // Create HTTP services
        TaskHttpService taskService = new TaskHttpService();
        ProjectHttpService projectService = new ProjectHttpService();
        SprintHttpService sprintService = new SprintHttpService();
        UserHttpService userService = new UserHttpService();
        BacklogHttpService backlogService = new BacklogHttpService();
        AuthHttpService authService = new AuthHttpService();

        // Create all 4 ViewModels
        TaskViewModel taskViewModel = new TaskViewModel();
        ProjectViewModel projectViewModel = new ProjectViewModel();
        SprintViewModel sprintViewModel = new SprintViewModel();
        UserViewModel userViewModel = new UserViewModel();

        // Create all 4 sub-controllers
        AuthController authController = new AuthController(authService, userService, userViewModel);
        TaskController taskController = new TaskController(taskService, taskViewModel, userViewModel);
        ProjectController projectController = new ProjectController(projectService, projectViewModel, userViewModel);
        SprintController sprintController = new SprintController(sprintService, sprintViewModel);

        // Create the GUIController wiring HTTP services + sub-controllers + UserViewModel
        GUIController gui = new GUIController(
                taskService, projectService, sprintService, userService, backlogService, authService,
                authController, taskController, projectController, sprintController, userViewModel);

        // Set session persistence for local mode
        if (local) {
            gui.setSessionPersistence(SessionDAO::load, SessionDAO::save);
        }

        // Initialize store (handles guest account, session loading, etc.)
        gui.initStore(local);

        // Dark theme: use cross-platform L&F for full color control, then apply dark defaults
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        TrakTheme.applyDefaults();

        // Show GUI immediately
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(
                    gui, taskViewModel, projectViewModel, sprintViewModel, userViewModel);
            mainFrame.setVisible(true);
        });

        // Seed data in background if requested
        if (seedTest) {
            new Thread(() -> {
                gui.seedData();
                gui.refreshAll();
            }, "seed-data").start();
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
