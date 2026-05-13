package task.trak.app.client.gui.view;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.SprintDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.app.client.gui.controller.TTAppGUI;
import task.trak.app.client.gui.model.CommandEvent;
import task.trak.app.client.gui.model.CommandEventType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final TTAppGUI app;
    private final ContentPanel contentPanel;
    private final StatusPanel statusPanel;
    private final ErrorPanel errorPanel;

    public MainFrame(TTAppGUI app) {
        super("Trak");
        this.app = app;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLayout(new BorderLayout());

        // --- Top section: status bar + nav bar + error panel ---
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        this.statusPanel = new StatusPanel(this::onCommandSubmitted);
        topSection.add(statusPanel);
        topSection.add(createNavBar());

        this.errorPanel = new ErrorPanel();
        topSection.add(errorPanel);

        add(topSection, BorderLayout.NORTH);

        // --- Center: content panel ---
        this.contentPanel = new ContentPanel();
        this.contentPanel.setOnCommand(this::onCommandSubmitted);
        add(contentPanel, BorderLayout.CENTER);

        // --- Bottom: command input ---
        CommandInputPanel inputPanel = new CommandInputPanel(this::onCommandSubmitted);
        add(inputPanel, BorderLayout.SOUTH);

        updateStatus();
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        navBar.setBorder(new EmptyBorder(2, 6, 2, 6));

        JButton tasksBtn = createNavButton("Tasks", "tasks");
        JButton projectsBtn = createNavButton("Projects", "projects");
        JButton sprintsBtn = createNavButton("Sprints", "sprints");

        navBar.add(tasksBtn);
        navBar.add(projectsBtn);
        navBar.add(sprintsBtn);

        return navBar;
    }

    private JButton createNavButton(String label, String command) {
        JButton btn = new JButton(label);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> onCommandSubmitted(command));
        return btn;
    }

    private void onCommandSubmitted(String command) {
        contentPanel.showCommand(command);
        app.executeCommand(command);
    }

    @SuppressWarnings("unchecked")
    public void displayCommandResult(CommandEvent event) {
        if (!event.success()) {
            errorPanel.showError(event.errorMessage());
            updateStatus();
            return;
        }

        Object data = event.data();
        CommandEventType type = event.type();

        // Route data-carrying events to the appropriate content view
        if (data instanceof List<?> list && !list.isEmpty()) {
            Object first = list.getFirst();
            if (first instanceof TaskDTO) {
                contentPanel.showTasks((List<TaskDTO>) data);
                updateStatus();
                return;
            }
            if (first instanceof ProjectDTO) {
                contentPanel.showProjects((List<ProjectDTO>) data);
                updateStatus();
                return;
            }
            if (first instanceof SprintDTO) {
                contentPanel.showSprints((List<SprintDTO>) data);
                updateStatus();
                return;
            }
        }

        // For known list types that returned empty, show the appropriate empty view
        if (type == CommandEventType.TASK_LIST) {
            contentPanel.showTasks(List.of());
            updateStatus();
            return;
        }
        if (type == CommandEventType.PROJECT_LIST) {
            contentPanel.showProjects(List.of());
            updateStatus();
            return;
        }
        if (type == CommandEventType.SPRINT_LIST) {
            contentPanel.showSprints(List.of());
            updateStatus();
            return;
        }

        // After mutations, refresh the relevant list view silently
        if (type == CommandEventType.PROJECT_CREATED || type == CommandEventType.PROJECT_UPDATED || type == CommandEventType.PROJECT_DELETED) {
            app.executeCommand("projects");
            updateStatus();
            return;
        }
        if (type == CommandEventType.TASK_CREATED || type == CommandEventType.TASK_UPDATED || type == CommandEventType.TASK_DELETED || type == CommandEventType.COMPLETE_TASK) {
            app.executeCommand("tasks");
            updateStatus();
            return;
        }
        if (type == CommandEventType.SPRINT_CREATED || type == CommandEventType.SPRINT_UPDATED || type == CommandEventType.SPRINT_DELETED) {
            app.executeCommand("sprints");
            updateStatus();
            return;
        }
        if (type == CommandEventType.LOGIN || type == CommandEventType.LOGOUT) {
            updateStatus();
            return;
        }

        // Fallback: show raw text output
        contentPanel.showOutput(event.textOutput());
        updateStatus();
    }

    private void updateStatus() {
        statusPanel.update(app.getSession());
    }
}
