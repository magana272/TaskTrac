package task.trak.app.client.gui.view;

import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.error.ErrorView;
import task.trak.app.client.gui.view.panel.StatusPanel;
import task.trak.app.client.gui.viewmodel.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements ViewModelChangeListener {

    private final GUIController controller;
    private final UserViewModel userViewModel;

    private final StatusPanel statusPanel;
    private final DashboardView dashboardView;

    public MainFrame(GUIController controller,
                     TaskViewModel taskViewModel,
                     ProjectViewModel projectViewModel,
                     SprintViewModel sprintViewModel,
                     UserViewModel userViewModel) {
        super("Trak");
        setUndecorated(true);
        this.controller = controller;
        this.userViewModel = userViewModel;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 700);
        setMinimumSize(new Dimension(800, 500));
        setLayout(new BorderLayout());
        getContentPane().setBackground(TrakTheme.BG_DARK);

        userViewModel.addObserver(this);

        // ── Top section: status bar ──
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(TrakTheme.BG_SURFACE);

        this.statusPanel = new StatusPanel(controller);
        topSection.add(statusPanel);

        add(topSection, BorderLayout.NORTH);

        // ── Center: Dashboard ──
        this.dashboardView = new DashboardView(controller, taskViewModel, projectViewModel, sprintViewModel);
        add(dashboardView, BorderLayout.CENTER);

        // ── Bottom: command input ──
        CommandInputPanel inputPanel = new CommandInputPanel(this::onCommandSubmitted);
        add(inputPanel, BorderLayout.SOUTH);

        updateStatus();
        setLocationRelativeTo(null);
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case SESSION -> {
                    updateStatus();
                    if (userViewModel.getSession() == null) {
                        controller.clearViewModels();
                    } else {
                        // Refresh all data off-EDT, fires notifications when done
                        new Thread(() -> controller.refreshAll(), "session-refresh").start();
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
                        System.err.println(output);
                    }
                }
                default -> {
                    // TASKS, PROJECTS, SPRINTS handled by DashboardView's own observer
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
