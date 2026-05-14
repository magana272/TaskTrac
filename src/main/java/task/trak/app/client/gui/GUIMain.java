package task.trak.app.client.gui;

import task.trak.api.service.ServiceFactory;
import task.trak.app.client.http.ApiClient;
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
            ServiceFactory.registerHttpServices();
        } else {
            if (!Files.exists(Path.of(TTApp.storedir)) || !Files.isDirectory(Path.of(TTApp.storedir))) {
                try {
                    Files.createDirectories(Path.of(TTApp.storedir));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create store directory: " + e.getMessage(), e);
                }
            }
            ServiceFactory.registerLocalServices();
        }

        // Create all 4 ViewModels
        TaskViewModel taskViewModel = new TaskViewModel();
        ProjectViewModel projectViewModel = new ProjectViewModel();
        SprintViewModel sprintViewModel = new SprintViewModel();
        UserViewModel userViewModel = new UserViewModel();

        // Create all 4 sub-controllers
        AuthController authController = new AuthController(userViewModel);
        TaskController taskController = new TaskController(taskViewModel, userViewModel);
        ProjectController projectController = new ProjectController(projectViewModel, userViewModel);
        SprintController sprintController = new SprintController(sprintViewModel);

        // Create the GUIController wiring sub-controllers + UserViewModel
        GUIController gui = new GUIController(
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

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(
                    gui, taskViewModel, projectViewModel, sprintViewModel, userViewModel);
            mainFrame.setVisible(true);
        });
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
