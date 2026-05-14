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
import task.trak.app.server.dao.SessionDAO;

import javax.swing.*;
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

        // Create and show the MainFrame on the EDT
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

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
