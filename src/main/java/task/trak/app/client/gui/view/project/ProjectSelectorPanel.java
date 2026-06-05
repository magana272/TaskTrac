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
    private final JComboBox<String> projectCombo;

    public ProjectSelectorPanel(GUIController controller) {
        this.controller = controller;
        this.projectViewModel = controller.getProjectController().getViewModel();

        setLayout(new FlowLayout(FlowLayout.LEFT, TrakTheme.SP_MD, 0));
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_XL, TrakTheme.SP_SM, TrakTheme.SP_XL));

        JLabel label = new JLabel("Project:");
        label.setFont(TrakTheme.FONT_TITLE);
        label.setForeground(TrakTheme.ACCENT);
        add(label);

        projectCombo = new JComboBox<>();
        TrakTheme.styleComboBox(projectCombo);
        projectCombo.setPreferredSize(new Dimension(200, 28));
        projectCombo.addActionListener(e -> {
            if (refreshing) return;
            String selected = (String) projectCombo.getSelectedItem();
            if (selected != null) {
                projectViewModel.setSelectedProject(selected);
            }
        });
        add(projectCombo);

        JButton addBtn = new JButton("+ Add Project");
        TrakTheme.styleButtonPrimary(addBtn);
        addBtn.setPreferredSize(new Dimension(130, 28));
        addBtn.addActionListener(e -> new ProjectCreateView(this, controller.getProjectController()).show());
        add(addBtn);
    }

    private boolean refreshing = false;

    public void refresh(List<ProjectDTO> projects) {
        refreshing = true;
        try {
            String current = projectViewModel.getSelectedProject();
            projectCombo.removeAllItems();
            projectCombo.addItem("All");
            for (ProjectDTO p : projects) {
                projectCombo.addItem(p.projectName());
            }
            // Restore selection if still valid
            for (int i = 0; i < projectCombo.getItemCount(); i++) {
                if (projectCombo.getItemAt(i).equals(current)) {
                    projectCombo.setSelectedIndex(i);
                    return;
                }
            }
            projectCombo.setSelectedIndex(0);
        } finally {
            refreshing = false;
        }
    }
}
