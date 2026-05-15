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
    private final JLabel statsLabel;
    private final JPanel progressBar;
    private final JButton addSprintBtn;
    private final JButton completeSprintBtn;
    private final JButton deleteSprintBtn;
    private double progress = 0;
    private SprintDTO activeSprint = null;

    public SprintProgressPanel(GUIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(TrakTheme.SP_MD, TrakTheme.SP_XS));
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_XL, TrakTheme.SP_MD, TrakTheme.SP_XL));

        // Top row: sprint name + add button
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        sprintLabel = new JLabel("No active sprint");
        sprintLabel.setFont(TrakTheme.FONT_HEADING);
        sprintLabel.setForeground(TrakTheme.TEXT_PRIMARY);

        addSprintBtn = new JButton("+ Add Sprint");
        TrakTheme.styleButtonPrimary(addSprintBtn);
        addSprintBtn.addActionListener(e -> {
            // Open sprint add dialog
            List<task.trak.model.dto.ProjectDTO> projects = controller.getProjectController().getViewModel().get();
            List<TaskDTO> tasks = controller.getTaskController().getViewModel().get();
            new SprintAddView(this, controller.getSprintController(), projects, tasks).show();
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
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Permanently delete sprint \"" + activeSprint.name() + "\" and remove all task associations?",
                    "Delete Sprint", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.getSprintController().deleteSprint(activeSprint.name());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(deleteSprintBtn);
        btnPanel.add(completeSprintBtn);
        btnPanel.add(addSprintBtn);

        topRow.add(sprintLabel, BorderLayout.WEST);
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
        // Find active (non-completed) sprint for selected project
        SprintDTO active = null;
        if (!"All".equals(selectedProject)) {
            for (SprintDTO s : sprints) {
                if (selectedProject.equals(s.projectName()) && !s.completed()) {
                    active = s;
                    break; // first non-completed sprint for this project
                }
            }
        } else {
            for (SprintDTO s : sprints) {
                if (!s.completed()) {
                    active = s;
                    break;
                }
            }
        }

        this.activeSprint = active;
        completeSprintBtn.setVisible(active != null);
        deleteSprintBtn.setVisible(active != null);

        if (active == null) {
            sprintLabel.setText("No active sprint");
            statsLabel.setText("");
            progress = 0;
            progressBar.repaint();
            return;
        }

        // Sprint header
        SimpleDateFormat fmt = new SimpleDateFormat("MMM dd");
        String dates = "";
        if (active.startDate() != null && active.endDate() != null) {
            dates = "  (" + fmt.format(active.startDate()) + " – " + fmt.format(active.endDate()) + ")";
        }
        sprintLabel.setText("Sprint: " + active.name() + dates);

        // Count tasks by status
        int total = 0, ready = 0, inProgress = 0, complete = 0;
        if (active.taskIds() != null) {
            for (Long taskId : active.taskIds()) {
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
        statsLabel.setText("Ready: " + ready + "  │  In Progress: " + inProgress
                + "  │  Complete: " + complete + "    " + complete + "/" + total + " tasks  " + pct + "%");
    }
}
