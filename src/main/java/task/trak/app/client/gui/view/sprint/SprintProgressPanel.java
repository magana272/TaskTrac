package task.trak.app.client.gui.view.sprint;

import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class SprintProgressPanel extends JPanel {

    private final GUIController controller;
    private final JLabel sprintLabel;
    private final JComboBox<String> sprintCombo;
    private final JLabel statsLabel;
    private final JPanel progressBar;
    private final JButton addSprintBtn;
    private final JButton completeSprintBtn;
    private final JButton deleteSprintBtn;
    private double progress = 0;
    private SprintDTO activeSprint = null;
    private List<SprintDTO> projectSprints = List.of();
    private boolean refreshing = false;

    public SprintProgressPanel(GUIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(TrakTheme.SP_MD, TrakTheme.SP_XS));
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_XL, TrakTheme.SP_MD, TrakTheme.SP_XL));

        // Top row: sprint label + dropdown + buttons
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, TrakTheme.SP_SM, 0));
        leftPanel.setOpaque(false);

        sprintLabel = new JLabel("Sprint:");
        sprintLabel.setFont(TrakTheme.FONT_HEADING);
        sprintLabel.setForeground(TrakTheme.TEXT_PRIMARY);

        sprintCombo = new JComboBox<>();
        TrakTheme.styleComboBox(sprintCombo);
        sprintCombo.setPreferredSize(new Dimension(200, 28));

        leftPanel.add(sprintLabel);
        leftPanel.add(sprintCombo);

        addSprintBtn = new JButton("+ Add Sprint");
        TrakTheme.styleButtonPrimary(addSprintBtn);
        addSprintBtn.addActionListener(e -> {
            // Open sprint add dialog
            List<task.trak.model.dto.ProjectDTO> projects = controller.getProjectController().getViewModel().get();
            List<TaskDTO> tasks = controller.getTaskController().getViewModel().get();
            String selected = controller.getProjectController().getViewModel().getSelectedProject();
            String locked = (selected != null && !"All".equals(selected)) ? selected : null;
            new SprintAddView(this, controller.getSprintController(), projects, tasks, locked).show();
        });

        completeSprintBtn = new JButton("Complete Sprint");
        TrakTheme.styleButtonNav(completeSprintBtn);
        completeSprintBtn.setVisible(false);
        completeSprintBtn.addActionListener(e -> {
            if (activeSprint == null) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Complete sprint \"" + activeSprint.name() + "\"? It will be archived.",
                    "Complete Sprint", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.getSprintController().completeSprint(activeSprint.name(), activeSprint.projectName());
            }
        });

        deleteSprintBtn = new JButton("Delete");
        TrakTheme.styleButtonNav(deleteSprintBtn);
        deleteSprintBtn.setVisible(false);
        deleteSprintBtn.addActionListener(e -> {
            if (activeSprint == null) return;
            showDeleteConfirmDialog(activeSprint);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(deleteSprintBtn);
        btnPanel.add(completeSprintBtn);
        btnPanel.add(addSprintBtn);

        sprintCombo.addActionListener(e -> {
            if (refreshing) return;
            int idx = sprintCombo.getSelectedIndex();
            if (idx >= 0 && idx < projectSprints.size()) {
                activeSprint = projectSprints.get(idx);
                completeSprintBtn.setVisible(!activeSprint.completed());
                deleteSprintBtn.setVisible(true);
                updateProgressDisplay();
            }
        });

        topRow.add(leftPanel, BorderLayout.WEST);
        topRow.add(btnPanel, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        // Progress bar (custom painted)
        progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Background track
                g2.setColor(TrakTheme.BG_ELEVATED);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                // Filled portion
                if (progress > 0) {
                    g2.setColor(TrakTheme.ACCENT_GREEN);
                    g2.fillRoundRect(0, 0, (int)(w * progress), h, 8, 8);
                }
                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 14));
        progressBar.setOpaque(false);
        add(progressBar, BorderLayout.CENTER);

        // Stats row
        statsLabel = new JLabel("");
        statsLabel.setFont(TrakTheme.FONT_SMALL);
        statsLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        add(statsLabel, BorderLayout.SOUTH);
    }

    public void refresh(List<SprintDTO> sprints, List<TaskDTO> allTasks, String selectedProject) {
        this.allTasks = allTasks;

        // Filter sprints for the selected project
        projectSprints = new java.util.ArrayList<>();
        for (SprintDTO s : sprints) {
            if ("All".equals(selectedProject) || selectedProject.equals(s.projectName())) {
                projectSprints.add(s);
            }
        }

        // Populate dropdown
        refreshing = true;
        try {
            String previousSelection = activeSprint != null ? activeSprint.name() : null;
            sprintCombo.removeAllItems();
            int selectIdx = -1;
            int firstActiveIdx = -1;
            for (int i = 0; i < projectSprints.size(); i++) {
                SprintDTO s = projectSprints.get(i);
                String label = s.name() + (s.completed() ? " (done)" : "");
                sprintCombo.addItem(label);
                if (s.name().equals(previousSelection)) {
                    selectIdx = i;
                }
                if (firstActiveIdx < 0 && !s.completed()) {
                    firstActiveIdx = i;
                }
            }

            if (projectSprints.isEmpty()) {
                activeSprint = null;
            } else if (selectIdx >= 0) {
                sprintCombo.setSelectedIndex(selectIdx);
                activeSprint = projectSprints.get(selectIdx);
            } else if (firstActiveIdx >= 0) {
                sprintCombo.setSelectedIndex(firstActiveIdx);
                activeSprint = projectSprints.get(firstActiveIdx);
            } else {
                sprintCombo.setSelectedIndex(0);
                activeSprint = projectSprints.get(0);
            }
        } finally {
            refreshing = false;
        }

        boolean hasSprint = activeSprint != null;
        sprintLabel.setVisible(hasSprint);
        sprintCombo.setVisible(hasSprint);
        completeSprintBtn.setVisible(hasSprint && !activeSprint.completed());
        deleteSprintBtn.setVisible(hasSprint);

        updateProgressDisplay();
    }

    private List<TaskDTO> allTasks = List.of();

    private void updateProgressDisplay() {
        if (activeSprint == null) {
            statsLabel.setText("No sprint");
            progress = 0;
            progressBar.repaint();
            return;
        }

        // Count tasks by status
        int total = 0, ready = 0, inProgress = 0, complete = 0;
        if (activeSprint.taskIds() != null) {
            for (Long taskId : activeSprint.taskIds()) {
                for (TaskDTO t : allTasks) {
                    if (t.id().equals(taskId)) {
                        total++;
                        if ("COMPLETE".equals(t.status())) complete++;
                        else if ("INPROGRESS".equals(t.status())) inProgress++;
                        else ready++;
                        break;
                    }
                }
            }
        }

        progress = total > 0 ? (double) complete / total : 0;
        progressBar.repaint();

        int pct = total > 0 ? (int)(progress * 100) : 0;
        SimpleDateFormat fmt = new SimpleDateFormat("MMM dd");
        String dates = "";
        if (activeSprint.startDate() != null && activeSprint.endDate() != null) {
            dates = "   " + fmt.format(activeSprint.startDate()) + " – " + fmt.format(activeSprint.endDate());
        }
        statsLabel.setText("Ready: " + ready + "  │  In Progress: " + inProgress
                + "  │  Complete: " + complete + "    " + complete + "/" + total + " tasks  " + pct + "%" + dates);
    }

    private void showDeleteConfirmDialog(SprintDTO sprint) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Delete Sprint", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel("Delete Sprint");
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("Permanently delete sprint \"" + sprint.name() + "\" and remove all task associations?");
        msgLabel.setFont(TrakTheme.FONT_BODY);
        msgLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        msgLabel.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(msgLabel, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        buttonRow.setBackground(TrakTheme.BG_SURFACE);
        buttonRow.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_MD, TrakTheme.SP_LG));

        JButton cancelBtn = new JButton("Cancel");
        TrakTheme.styleButtonNav(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(80, 28));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton deleteBtn = new JButton("Delete");
        TrakTheme.styleButtonPrimary(deleteBtn);
        deleteBtn.setPreferredSize(new Dimension(80, 28));
        deleteBtn.addActionListener(e -> {
            dialog.dispose();
            controller.getSprintController().deleteSprint(sprint.name());
        });

        buttonRow.add(cancelBtn);
        buttonRow.add(deleteBtn);
        dialog.add(buttonRow, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
