package task.trak.app.client.gui.view.task;

import task.trak.model.dto.TaskDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.DataView;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.viewmodel.ViewModelChangeListener;
import task.trak.app.client.gui.viewmodel.ViewModelChangeType;

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

            JPanel gridPanel = new JPanel(new GridBagLayout());
            gridPanel.setBackground(TrakTheme.BG_DARK);

            Map<String, List<String>> memberCache = new HashMap<>();

            List<JComponent> cards = new ArrayList<>();
            for (TaskDTO task : visible) {
                List<String> assignees = memberCache.computeIfAbsent(
                        task.projectName(),
                        name -> guiController.getProjectController().getProjectMembers(name));
                cards.add(new TaskCardPanel(task, guiController.getTaskController(), assignees));
            }

            if (cards.isEmpty()) {
                JLabel emptyLabel = new JLabel("  All tasks completed. Toggle above to view.");
                emptyLabel.setForeground(TrakTheme.TEXT_SECONDARY);
                emptyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
                cards.add(emptyLabel);
            }

            layoutCards(gridPanel, cards);
            taskCardsContainer.add(gridPanel, BorderLayout.CENTER);

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

    public void showTasks(List<TaskDTO> tasks) {
        guiController.getTaskController().getViewModel().setAll(tasks);
    }

    private JPanel buildToolbar(List<TaskDTO> allTasks, long completedCount) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, TrakTheme.SP_SM, TrakTheme.SP_SM));
        toolbar.setBackground(TrakTheme.BG_SURFACE);
        toolbar.setBorder(TrakTheme.pad(TrakTheme.SP_SM, TrakTheme.SP_XL));

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

        toolbar.add(Box.createHorizontalStrut(8));

        // Add Task
        JButton addBtn = new JButton("+ Add Task");
        TrakTheme.styleButtonPrimary(addBtn);
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
        int minCardWidth = 320;
        int gap = TrakTheme.SP_SM;
        int containerWidth = taskCardsContainer.getWidth() - gap;
        if (containerWidth <= 0) containerWidth = 900;
        int cols = Math.max(1, (containerWidth + gap) / (minCardWidth + gap));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(gap / 2, gap / 2, gap / 2, gap / 2);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        for (int i = 0; i < cards.size(); i++) {
            gbc.gridx = i % cols;
            gbc.gridy = i / cols;
            gbc.weighty = 0;
            panel.add(cards.get(i), gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = (cards.size() / cols) + 1;
        gbc.gridwidth = cols;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);
    }
}
