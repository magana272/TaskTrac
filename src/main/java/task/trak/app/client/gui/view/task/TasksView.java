package task.trak.app.client.gui.view.task;

import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.DataView;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.viewmodel.ViewModelChangeListener;
import task.trak.app.client.gui.viewmodel.ViewModelChangeType;

import task.trak.model.util.TimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * Displays task cards in a responsive grid with a toolbar for filtering/sorting.
 */
public class TasksView extends DataView implements ViewModelChangeListener {

    private final GUIController guiController;
    private final JPanel taskCardsContainer;
    private javax.swing.Timer timerTick;
    private long lastRefreshTimestamp;
    private List<TaskCardPanel> activeCards = new ArrayList<>();

    public TasksView(GUIController guiController) {
        this.guiController = guiController;

        setLayout(new BorderLayout());
        setBackground(TrakTheme.BG_DARK);

        taskCardsContainer = new JPanel(new BorderLayout());
        taskCardsContainer.setBackground(TrakTheme.BG_DARK);
        add(taskCardsContainer, BorderLayout.CENTER);

        guiController.getTaskController().getViewModel().addObserver(this);
        guiController.getProjectController().getViewModel().addObserver(this);
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        if (type == ViewModelChangeType.TASKS) {
            SwingUtilities.invokeLater(this::render);
        }
    }

    @Override
    public void render() {
        taskCardsContainer.removeAll();

        for (var l : taskCardsContainer.getComponentListeners()) {
            taskCardsContainer.removeComponentListener(l);
        }

        List<TaskDTO> allTasks = guiController.getTaskController().getViewModel().get();
        List<TaskDTO> visible = guiController.getTaskController().getViewModel().getFiltered();

        long completedCount = (allTasks != null)
                ? allTasks.stream().filter(t -> "COMPLETE".equals(t.status())).count()
                : 0;

        if (visible.isEmpty() && completedCount == 0) {
            taskCardsContainer.setLayout(new BorderLayout());
            JButton addBtn = new JButton("+ Add a Task");
            TrakTheme.styleButtonPrimary(addBtn);
            addBtn.addActionListener(e -> showAddTaskDialog());
            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.setBackground(TrakTheme.BG_DARK);
            placeholder.add(addBtn);
            taskCardsContainer.add(placeholder, BorderLayout.CENTER);
        } else {
            taskCardsContainer.setLayout(new BorderLayout());

            JPanel toolbar = buildToolbar(allTasks, completedCount);
            taskCardsContainer.add(toolbar, BorderLayout.NORTH);

            int gap = TrakTheme.SP_SM;
            int minCardWidth = 280;

            // Scrollable grid panel — cards flex to fill row (like CSS flex: 1, flex-wrap: wrap)
            @SuppressWarnings("serial")
            class FlexGridPanel extends JPanel implements Scrollable {
                private final int gap;
                private final int minCardW;

                FlexGridPanel(int gap, int minCardW) {
                    super(new GridBagLayout());
                    this.gap = gap;
                    this.minCardW = minCardW;
                }

                void layoutCards(List<JComponent> cards) {
                    removeAll();
                    int w = getWidth();
                    if (w <= 0) w = getParent() != null ? getParent().getWidth() : 0;
                    if (w <= 0) w = 900;
                    int cols = Math.max(2, (w + gap) / (minCardW + gap));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(gap / 2, gap / 2, gap / 2, gap / 2);
                    gbc.anchor = GridBagConstraints.NORTH;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.weightx = 1.0;

                    for (int i = 0; i < cards.size(); i++) {
                        gbc.gridx = i % cols;
                        gbc.gridy = i / cols;
                        gbc.weighty = 0;
                        add(cards.get(i), gbc);
                    }

                    // Vertical glue to push cards to the top
                    gbc.gridx = 0;
                    gbc.gridy = (cards.size() / cols) + 1;
                    gbc.gridwidth = cols;
                    gbc.weighty = 1.0;
                    gbc.fill = GridBagConstraints.VERTICAL;
                    add(Box.createGlue(), gbc);

                    revalidate();
                    repaint();
                }

                @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
                @Override public int getScrollableUnitIncrement(Rectangle vr, int o, int d) { return 16; }
                @Override public int getScrollableBlockIncrement(Rectangle vr, int o, int d) { return vr.height; }
                @Override public boolean getScrollableTracksViewportWidth() { return true; }
                @Override public boolean getScrollableTracksViewportHeight() { return false; }
            }

            FlexGridPanel gridPanel = new FlexGridPanel(gap, minCardWidth);
            gridPanel.setBackground(TrakTheme.BG_DARK);

            // Build task-id -> sprint-name lookup
            Map<Long, String> taskSprintMap = new HashMap<>();
            for (SprintDTO sprint : guiController.getSprintController().getViewModel().get()) {
                if (sprint.taskIds() != null) {
                    for (Long tid : sprint.taskIds()) {
                        taskSprintMap.putIfAbsent(tid, sprint.name());
                    }
                }
            }

            Map<String, List<String>> memberCache = new HashMap<>();
            this.activeCards = new ArrayList<>();
            List<JComponent> cards = new ArrayList<>();

            long now = System.currentTimeMillis();
            for (TaskDTO task : visible) {
                List<String> assignees = memberCache.computeIfAbsent(
                        task.projectName(),
                        name -> guiController.getProjectController().getProjectMembers(name));
                String sprintName = taskSprintMap.get(task.id());
                TaskCardPanel card = new TaskCardPanel(task, guiController.getTaskController(), assignees, sprintName);

                // Initialize timer state immediately for in-progress tasks
                if ("INPROGRESS".equals(task.status()) && task.estimate() != null) {
                    long elapsed = task.timeSpentMs() + (now - lastRefreshTimestamp);
                    long estimateMs = TimeUtil.parseDurationToMs(task.estimate());
                    double ratio = estimateMs > 0 ? (double) elapsed / estimateMs : -1;
                    card.setTimerState(ratio, elapsed);
                }

                cards.add(card);
                this.activeCards.add(card);
            }

            if (cards.isEmpty()) {
                JLabel emptyLabel = new JLabel("  All tasks completed. Toggle above to view.");
                emptyLabel.setForeground(TrakTheme.TEXT_SECONDARY);
                emptyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
                cards.add(emptyLabel);
            }

            gridPanel.layoutCards(cards);

            JScrollPane scrollPane = new JScrollPane(gridPanel);
            TrakTheme.styleScrollPane(scrollPane);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Re-layout on viewport resize so columns adjust
            scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    gridPanel.layoutCards(cards);
                }
            });

            taskCardsContainer.add(scrollPane, BorderLayout.CENTER);

            // Only reset timestamp on first load (when no active timer exists)
            if (lastRefreshTimestamp == 0) {
                lastRefreshTimestamp = System.currentTimeMillis();
            }
            startTimerIfNeeded();
        }

        taskCardsContainer.revalidate();
        taskCardsContainer.repaint();
    }

    public void showTasks(List<TaskDTO> tasks) {
        guiController.getTaskController().getViewModel().setAll(tasks);
    }

    private JPanel buildToolbar(List<TaskDTO> allTasks, long completedCount) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, TrakTheme.SP_MD, 0));
        toolbar.setBackground(TrakTheme.BG_SURFACE);
        toolbar.setBorder(TrakTheme.pad(TrakTheme.SP_SM, TrakTheme.SP_XL));

        // Add Task (first)
        JButton addBtn = new JButton("+ Add Task");
        TrakTheme.styleButtonPrimary(addBtn);
        addBtn.setPreferredSize(new Dimension(130, 28));
        addBtn.addActionListener(e -> showAddTaskDialog());
        toolbar.add(addBtn);

        // Sort
        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setForeground(TrakTheme.TEXT_SECONDARY);

        toolbar.add(sortLabel);
        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"None", "Due Date", "Estimate"});
        sortCombo.setSelectedItem(guiController.getTaskController().getViewModel().getSort());
        sortCombo.addActionListener(e -> {
            String selected = (String) sortCombo.getSelectedItem();
            if (selected != null) {
                guiController.getTaskController().getViewModel().setSort(selected);
            }
        });
        TrakTheme.styleComboBox(sortCombo);
        toolbar.add(sortCombo);

        toolbar.add(Box.createHorizontalStrut(8));

        // Show completed
        JCheckBox archiveToggle = new JCheckBox("Show completed (" + completedCount + ")");
        archiveToggle.setSelected(guiController.getTaskController().getViewModel().isShowCompleted());
        archiveToggle.setOpaque(false);
        archiveToggle.setForeground(TrakTheme.TEXT_SECONDARY);
        archiveToggle.addActionListener(e -> guiController.getTaskController().getViewModel().setShowCompleted(archiveToggle.isSelected()));
        toolbar.add(archiveToggle);

        return toolbar;
    }

    private void showAddTaskDialog() {
        if (guiController.getAuthController().getSession() == null) {
            new task.trak.app.client.gui.view.auth.AuthPromptView(
                    this, guiController.getAuthController(), "add a task").show();
            return;
        }
        var projects = guiController.getProjectController().getProjectsForUser(
                guiController.getAuthController().getSession().getLogged_in_user());
        if (projects == null || projects.isEmpty()) {
            new task.trak.app.client.gui.view.error.ErrorView("No projects found. Create a project first.").show(this);
            return;
        }
        String selected = guiController.getProjectController().getViewModel().getSelectedProject();
        if (selected != null && !"All".equals(selected)) {
            // Find the index of the selected project and lock the dropdown
            for (int i = 0; i < projects.size(); i++) {
                if (selected.equals(projects.get(i).projectName())) {
                    new TaskAddView(SwingUtilities.getWindowAncestor(this),
                            guiController.getTaskController(), projects, i, true).show();
                    return;
                }
            }
        }
        new TaskAddView(SwingUtilities.getWindowAncestor(this), guiController.getTaskController(), projects).show();
    }

    private void startTimerIfNeeded() {
        boolean hasInProgress = activeCards.stream().anyMatch(TaskCardPanel::isInProgress);
        if (hasInProgress && timerTick == null) {
            timerTick = new javax.swing.Timer(1000, e -> updateTimers());
            timerTick.start();
        } else if (!hasInProgress && timerTick != null) {
            timerTick.stop();
            timerTick = null;
        }
    }

    private void updateTimers() {
        long now = System.currentTimeMillis();
        long offset = now - lastRefreshTimestamp;
        for (TaskCardPanel card : activeCards) {
            if (card.isInProgress()) {
                TaskDTO t = card.getTask();
                long elapsed = t.timeSpentMs() + offset;
                long estimateMs = TimeUtil.parseDurationToMs(t.estimate());
                double ratio = estimateMs > 0 ? (double) elapsed / estimateMs : -1;
                card.setTimerState(ratio, elapsed);
                card.repaint();
            }
        }
    }
}
