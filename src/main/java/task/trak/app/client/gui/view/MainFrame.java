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
    private JButton tasksBtn;
    private JButton projectsBtn;
    private JButton sprintsBtn;
    private JButton activeNavBtn;

    public MainFrame(GUIController controller,
                     TaskViewModel taskViewModel,
                     ProjectViewModel projectViewModel,
                     SprintViewModel sprintViewModel,
                     UserViewModel userViewModel) {
        super("Trak");
        this.controller = controller;
        this.userViewModel = userViewModel;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 700);
        setMinimumSize(new Dimension(800, 500));
        setLayout(new BorderLayout());
        getContentPane().setBackground(TrakTheme.BG_DARK);

        taskViewModel.addObserver(this);
        projectViewModel.addObserver(this);
        sprintViewModel.addObserver(this);
        userViewModel.addObserver(this);

        // ── Top section: status bar + nav ──
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(TrakTheme.BG_SURFACE);

        this.statusPanel = new StatusPanel(controller);
        topSection.add(statusPanel);

        // Subtle divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(TrakTheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        topSection.add(sep);

        topSection.add(createNavBar());
        setNavButtonsEnabled(false);

        add(topSection, BorderLayout.NORTH);

        // ── Center: CardLayout with views ──
        this.cardLayout = new CardLayout();
        this.cardContainer = new JPanel(cardLayout);
        this.cardContainer.setBackground(TrakTheme.BG_DARK);

        this.tasksView = new TasksView(controller);
        this.projectsView = new ProjectsView(controller);
        this.sprintView = new SprintView(controller);

        this.outputPanel = new OutputPanel();
        JScrollPane outputScroll = new JScrollPane(outputPanel);
        outputScroll.setBorder(BorderFactory.createEmptyBorder());
        outputScroll.getViewport().setBackground(TrakTheme.BG_DARK);

        cardContainer.add(tasksView, CARD_TASKS);
        cardContainer.add(projectsView, CARD_PROJECTS);
        cardContainer.add(sprintView, CARD_SPRINTS);
        cardContainer.add(outputScroll, CARD_OUTPUT);

        cardLayout.show(cardContainer, CARD_OUTPUT);
        add(cardContainer, BorderLayout.CENTER);

        // ── Bottom: command input ──
        CommandInputPanel inputPanel = new CommandInputPanel(this::onCommandSubmitted);
        add(inputPanel, BorderLayout.SOUTH);

        updateStatus();
        setLocationRelativeTo(null);
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(TrakTheme.BG_SURFACE);
        navBar.setBorder(new EmptyBorder(TrakTheme.SP_XS, TrakTheme.SP_XL, TrakTheme.SP_XS, TrakTheme.SP_XL));

        // Left: view navigation
        JPanel navGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        navGroup.setOpaque(false);

        tasksBtn = createNavButton("Tasks", CARD_TASKS);
        projectsBtn = createNavButton("Projects", CARD_PROJECTS);
        sprintsBtn = createNavButton("Sprints", CARD_SPRINTS);

        navGroup.add(projectsBtn);
        navGroup.add(tasksBtn);
        navGroup.add(sprintsBtn);

        navBar.add(navGroup, BorderLayout.WEST);
        return navBar;
    }

    private JButton createNavButton(String label, String card) {
        JButton btn = new JButton(label);
        btn.setFont(TrakTheme.FONT_BODY);
        btn.setForeground(TrakTheme.TEXT_MUTED);
        btn.setBackground(TrakTheme.BG_SURFACE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(TrakTheme.SP_XS + 2, TrakTheme.SP_MD, TrakTheme.SP_XS + 2, TrakTheme.SP_MD));

        btn.addActionListener(e -> {
            switch (card) {
                case CARD_TASKS -> controller.getTaskController().refreshTasks();
                case CARD_PROJECTS -> controller.getProjectController().refreshProjects();
                case CARD_SPRINTS -> controller.getSprintController().refreshSprints();
            }
            cardLayout.show(cardContainer, card);
            setActiveNav(btn);
        });

        return btn;
    }

    private void setActiveNav(JButton btn) {
        // Reset previous
        if (activeNavBtn != null) {
            activeNavBtn.setForeground(TrakTheme.TEXT_MUTED);
            activeNavBtn.setBackground(TrakTheme.BG_SURFACE);
        }
        activeNavBtn = btn;
        if (btn != null) {
            btn.setForeground(TrakTheme.ACCENT);
            btn.setBackground(TrakTheme.BG_ELEVATED);
        }
    }

    private void setNavButtonsEnabled(boolean enabled) {
        tasksBtn.setEnabled(enabled);
        projectsBtn.setEnabled(enabled);
        sprintsBtn.setEnabled(enabled);
        if (!enabled) {
            setActiveNav(null);
        }
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case TASKS -> {
                    if (userViewModel.getSession() == null) return;
                    cardLayout.show(cardContainer, CARD_TASKS);
                    setActiveNav(tasksBtn);
                    tasksView.render();
                }
                case PROJECTS -> {
                    if (userViewModel.getSession() == null) return;
                    cardLayout.show(cardContainer, CARD_PROJECTS);
                    setActiveNav(projectsBtn);
                    projectsView.render();
                }
                case SPRINTS -> {
                    if (userViewModel.getSession() == null) return;
                    cardLayout.show(cardContainer, CARD_SPRINTS);
                    setActiveNav(sprintsBtn);
                    sprintView.render();
                }
                case SESSION -> {
                    updateStatus();
                    if (userViewModel.getSession() == null) {
                        controller.clearViewModels();
                        setNavButtonsEnabled(false);
                        cardLayout.show(cardContainer, CARD_OUTPUT);
                    } else {
                        setNavButtonsEnabled(true);
                    }
                }
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
