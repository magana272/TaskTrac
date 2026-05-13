package task.trak.app.client.gui.view.task;

import task.trak.api.dto.TaskDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.DataView;
import task.trak.app.client.gui.viewmodel.ViewModelChangeListener;
import task.trak.app.client.gui.viewmodel.ViewModelChangeType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays task cards in a responsive grid with a toolbar for filtering/sorting.
 * Replaces the task card grid portion previously in ContentPanel.
 */
public class TasksView extends DataView implements ViewModelChangeListener {

    private static final Color BG_COLOR = new Color(0xF5, 0xF5, 0xF5);

    private final GUIController guiController;
    private final JPanel taskCardsContainer;

    public TasksView(GUIController guiController) {
        this.guiController = guiController;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        taskCardsContainer = new JPanel(new BorderLayout());
        taskCardsContainer.setBackground(BG_COLOR);
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

    /**
     * Called by MainFrame or externally to trigger a render using current ViewModel state.
     */
    @Override
    public void render() {
        taskCardsContainer.removeAll();

        // Remove old resize listeners
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
            addBtn.addActionListener(e -> showAddTaskDialog());
            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.add(addBtn);
            taskCardsContainer.add(placeholder, BorderLayout.CENTER);
        } else {
            taskCardsContainer.setLayout(new BorderLayout());

            // Toolbar with filters, sort, toggle, add
            JPanel toolbar = buildToolbar(allTasks, completedCount);
            taskCardsContainer.add(toolbar, BorderLayout.NORTH);

            // Card grid
            JPanel gridPanel = new JPanel(new GridBagLayout());
            gridPanel.setBackground(BG_COLOR);

            List<JComponent> cards = new ArrayList<>();
            for (TaskDTO task : visible) {
                cards.add(new TaskCardPanel(task, guiController.getTaskController(), List.of()));
            }

            if (cards.isEmpty()) {
                JLabel emptyLabel = new JLabel("  All tasks completed. Toggle above to view.");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
                cards.add(emptyLabel);
            }

            layoutCards(gridPanel, cards);

            JScrollPane scroll = new JScrollPane(gridPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            taskCardsContainer.add(scroll, BorderLayout.CENTER);

            taskCardsContainer.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    layoutCards(gridPanel, cards);
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            });
        }

        taskCardsContainer.revalidate();
        taskCardsContainer.repaint();
    }

    /**
     * Provide tasks externally and render. Stores data in ViewModel then renders.
     */
    public void showTasks(List<TaskDTO> tasks) {
        guiController.getTaskController().getViewModel().setAll(tasks);
    }

    private JPanel buildToolbar(List<TaskDTO> allTasks, long completedCount) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolbar.setBackground(BG_COLOR);

        // Project filter
        Set<String> projectNames = new LinkedHashSet<>();
        projectNames.add("All");
        if (allTasks != null) {
            for (TaskDTO t : allTasks) {
                if (t.projectName() != null) projectNames.add(t.projectName());
            }
        }
        toolbar.add(new JLabel("Project:"));
        JComboBox<String> projectFilter = new JComboBox<>(projectNames.toArray(new String[0]));
        projectFilter.setSelectedItem(guiController.getTaskController().getViewModel().getProjectFilter());
        projectFilter.addActionListener(e -> {
            String selected = (String) projectFilter.getSelectedItem();
            if (selected != null) {
                guiController.getTaskController().getViewModel().setProjectFilter(selected);
            }
        });
        toolbar.add(projectFilter);

        toolbar.add(Box.createHorizontalStrut(8));

        // Sort
        toolbar.add(new JLabel("Sort:"));
        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"None", "Due Date", "Estimate"});
        sortCombo.setSelectedItem(guiController.getTaskController().getViewModel().getSort());
        sortCombo.addActionListener(e -> {
            String selected = (String) sortCombo.getSelectedItem();
            if (selected != null) {
                guiController.getTaskController().getViewModel().setSort(selected);
            }
        });
        toolbar.add(sortCombo);

        toolbar.add(Box.createHorizontalStrut(8));

        // Show completed
        JCheckBox archiveToggle = new JCheckBox("Show completed (" + completedCount + ")");
        archiveToggle.setSelected(guiController.getTaskController().getViewModel().isShowCompleted());
        archiveToggle.setOpaque(false);
        archiveToggle.addActionListener(e -> guiController.getTaskController().getViewModel().setShowCompleted(archiveToggle.isSelected()));
        toolbar.add(archiveToggle);

        toolbar.add(Box.createHorizontalStrut(8));

        // Add Task
        JButton addBtn = new JButton("+ Add Task");
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> showAddTaskDialog());
        toolbar.add(addBtn);

        return toolbar;
    }

    private void showAddTaskDialog() {
        var projects = guiController.getProjectController().getProjectsForUser(
                guiController.getSession() != null ? guiController.getSession().getLogged_in_user() : null);
        if (projects == null || projects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No projects found. Create a project first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new TaskAddView(SwingUtilities.getWindowAncestor(this), guiController.getTaskController(), projects).show();
    }

    private void layoutCards(JPanel panel, List<JComponent> cards) {
        panel.removeAll();
        int minCardWidth = 240;
        int gap = 10;
        int containerWidth = taskCardsContainer.getWidth() - gap;
        if (containerWidth <= 0) containerWidth = 900;
        int cols = Math.max(1, (containerWidth + gap) / (minCardWidth + gap));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(gap / 2, gap / 2, gap / 2, gap / 2);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        for (int i = 0; i < cards.size(); i++) {
            gbc.gridx = i % cols;
            gbc.gridy = i / cols;
            gbc.weighty = 0;
            panel.add(cards.get(i), gbc);
        }

        // Vertical filler to push cards to top
        gbc.gridx = 0;
        gbc.gridy = (cards.size() / cols) + 1;
        gbc.gridwidth = cols;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);
    }
}
