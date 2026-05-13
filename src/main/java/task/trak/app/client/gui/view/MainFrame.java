package task.trak.app.client.gui.view;

import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.error.ErrorView;
import task.trak.app.client.gui.view.panel.OutputPanel;
import task.trak.app.client.gui.view.panel.StatusPanel;
import task.trak.app.client.gui.view.project.ProjectsView;
import task.trak.app.client.gui.view.sprint.SprintView;
import task.trak.app.client.gui.view.task.TasksView;
import task.trak.app.client.gui.viewmodel.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame implements ViewModelChangeListener {

    private static final String CARD_TASKS = "tasks";
    private static final String CARD_PROJECTS = "projects";
    private static final String CARD_SPRINTS = "sprints";
    private static final String CARD_OUTPUT = "output";

    private final GUIController controller;
    private final UserViewModel userViewModel;

    private final StatusPanel statusPanel;
    private final CardLayout cardLayout;
    private final JPanel cardContainer;

    private final TasksView tasksView;
    private final ProjectsView projectsView;
    private final SprintView sprintView;
    private final OutputPanel outputPanel;

    public MainFrame(GUIController controller,
                     TaskViewModel taskViewModel,
                     ProjectViewModel projectViewModel,
                     SprintViewModel sprintViewModel,
                     UserViewModel userViewModel) {
        super("Trak");
        this.controller = controller;
        this.userViewModel = userViewModel;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLayout(new BorderLayout());

        taskViewModel.addObserver(this);
        projectViewModel.addObserver(this);
        sprintViewModel.addObserver(this);
        userViewModel.addObserver(this);

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        this.statusPanel = new StatusPanel(controller);
        topSection.add(statusPanel);
        topSection.add(createNavBar());

        add(topSection, BorderLayout.NORTH);

        // --- Center: CardLayout with views ---
        this.cardLayout = new CardLayout();
        this.cardContainer = new JPanel(cardLayout);

        this.tasksView = new TasksView(controller);
        this.projectsView = new ProjectsView(controller);
        this.sprintView = new SprintView(controller);

        this.outputPanel = new OutputPanel();
        JScrollPane outputScroll = new JScrollPane(outputPanel);
        outputScroll.setBorder(BorderFactory.createEmptyBorder());

        cardContainer.add(tasksView, CARD_TASKS);
        cardContainer.add(projectsView, CARD_PROJECTS);
        cardContainer.add(sprintView, CARD_SPRINTS);
        cardContainer.add(outputScroll, CARD_OUTPUT);

        cardLayout.show(cardContainer, CARD_OUTPUT);
        add(cardContainer, BorderLayout.CENTER);

        // --- Bottom: command input ---
        CommandInputPanel inputPanel = new CommandInputPanel(this::onCommandSubmitted);
        add(inputPanel, BorderLayout.SOUTH);

        updateStatus();
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        navBar.setBorder(new EmptyBorder(2, 6, 2, 6));

        JButton tasksBtn = new JButton("Tasks");
        tasksBtn.setFocusPainted(false);
        tasksBtn.addActionListener(e -> {
            controller.getTaskController().refreshTasks();
            cardLayout.show(cardContainer, CARD_TASKS);
        });

        JButton projectsBtn = new JButton("Projects");
        projectsBtn.setFocusPainted(false);
        projectsBtn.addActionListener(e -> {
            controller.getProjectController().refreshProjects();
            cardLayout.show(cardContainer, CARD_PROJECTS);
        });

        JButton sprintsBtn = new JButton("Sprints");
        sprintsBtn.setFocusPainted(false);
        sprintsBtn.addActionListener(e -> {
            controller.getSprintController().refreshSprints();
            cardLayout.show(cardContainer, CARD_SPRINTS);
        });

        navBar.add(tasksBtn);
        navBar.add(projectsBtn);
        navBar.add(sprintsBtn);

        return navBar;
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case TASKS -> {
                    cardLayout.show(cardContainer, CARD_TASKS);
                    tasksView.render();
                }
                case PROJECTS -> {
                    cardLayout.show(cardContainer, CARD_PROJECTS);
                    projectsView.render();
                }
                case SPRINTS -> {
                    cardLayout.show(cardContainer, CARD_SPRINTS);
                    sprintView.render();
                }
                case SESSION -> updateStatus();
                case ERROR -> {
                    String error = userViewModel.getLastError();
                    if (error != null) {
                        new ErrorView(error).show(this);
                    }
                }
                case OUTPUT -> {
                    String output = userViewModel.getLastOutput();
                    if (output != null) {
                        outputPanel.appendOutput(output);
                    }
                    cardLayout.show(cardContainer, CARD_OUTPUT);
                }
            }
        });
    }

    private void onCommandSubmitted(String command) {
        controller.executeCommand(command);
    }

    private void updateStatus() {
        statusPanel.update(userViewModel.getSession());
    }
}
