package task.trak.app.client.gui.view.project;

import task.trak.model.dto.ProjectDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.viewmodel.ProjectViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProjectSelectorPanel extends JPanel {

    private final GUIController controller;
    private final ProjectViewModel projectViewModel;
    private final JLabel projectLabel;
    private final JComboBox<String> projectCombo;
    private JButton deleteBtn;

    public ProjectSelectorPanel(GUIController controller) {
        this.controller = controller;
        this.projectViewModel = controller.getProjectController().getViewModel();

        setLayout(new FlowLayout(FlowLayout.LEFT, TrakTheme.SP_MD, 0));
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_XL, TrakTheme.SP_SM, TrakTheme.SP_XL));

        projectLabel = new JLabel("Project:");
        projectLabel.setFont(TrakTheme.FONT_TITLE);
        projectLabel.setForeground(TrakTheme.ACCENT);
        add(projectLabel);

        projectCombo = new JComboBox<>();
        TrakTheme.styleComboBox(projectCombo);
        projectCombo.setPreferredSize(new Dimension(200, 28));
        projectCombo.addActionListener(e -> {
            if (refreshing) return;
            String selected = (String) projectCombo.getSelectedItem();
            if (selected != null) {
                projectViewModel.setSelectedProject(selected);
                if (deleteBtn != null) {
                    deleteBtn.setVisible(!"All".equals(selected));
                }
            }
        });
        add(projectCombo);

        JButton addBtn = new JButton("+ Add Project");
        TrakTheme.styleButtonPrimary(addBtn);
        addBtn.setPreferredSize(new Dimension(130, 28));
        addBtn.addActionListener(e -> {
            if (controller.getAuthController().getSession() == null) {
                new task.trak.app.client.gui.view.auth.AuthPromptView(
                        this, controller.getAuthController(), "create a project").show();
                return;
            }
            new ProjectCreateView(this, controller.getProjectController()).show();
        });
        add(addBtn);

        deleteBtn = new JButton("Delete Project");
        TrakTheme.styleButtonNav(deleteBtn);
        deleteBtn.setPreferredSize(new Dimension(120, 28));
        deleteBtn.setVisible(false);
        deleteBtn.addActionListener(e -> {
            String selected = projectViewModel.getSelectedProject();
            if (selected == null || "All".equals(selected)) return;
            showDeleteConfirmDialog(selected);
        });
        add(deleteBtn);
    }

    private boolean refreshing = false;

    public void refresh(List<ProjectDTO> projects) {
        boolean hasProjects = !projects.isEmpty();
        projectLabel.setVisible(hasProjects);
        projectCombo.setVisible(hasProjects);
        refreshing = true;
        try {
            String current = projectViewModel.getSelectedProject();
            projectCombo.removeAllItems();
            projectCombo.addItem("All");
            for (ProjectDTO p : projects) {
                projectCombo.addItem(p.projectName());
            }
            // Restore selection if still valid
            boolean restored = false;
            for (int i = 0; i < projectCombo.getItemCount(); i++) {
                if (projectCombo.getItemAt(i).equals(current)) {
                    projectCombo.setSelectedIndex(i);
                    restored = true;
                    break;
                }
            }
            if (!restored) {
                projectCombo.setSelectedIndex(0);
            }
            String selected = (String) projectCombo.getSelectedItem();
            deleteBtn.setVisible(selected != null && !"All".equals(selected));
        } finally {
            refreshing = false;
        }
    }

    private void showDeleteConfirmDialog(String projectName) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Delete Project", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel("Delete Project");
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("<html>Permanently delete project \"" + projectName
                + "\" and all its tasks and sprints?</html>");
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

        JButton confirmBtn = new JButton("Delete");
        TrakTheme.styleButtonPrimary(confirmBtn);
        confirmBtn.setPreferredSize(new Dimension(80, 28));
        confirmBtn.addActionListener(e -> {
            dialog.dispose();
            controller.getProjectController().deleteProject(projectName);
            controller.getSprintController().refreshSprints();
            controller.getTaskController().refreshTasks();
        });

        buttonRow.add(cancelBtn);
        buttonRow.add(confirmBtn);
        dialog.add(buttonRow, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
