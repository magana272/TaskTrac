package task.trak.app.client.gui.view.project;

import task.trak.app.client.gui.controller.ProjectController;
import task.trak.app.client.gui.view.form.FormDialogView;

import javax.swing.*;
import java.awt.*;

public class ProjectCreateView extends FormDialogView {

    private final ProjectController projectController;

    private JTextField nameField;
    private JTextField summaryField;

    public ProjectCreateView(Component parent, ProjectController projectController) {
        super(parent, "Create Project");
        this.projectController = projectController;
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 4, 4));
        nameField = new JTextField();
        summaryField = new JTextField();
        panel.add(new JLabel("Project Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Summary:"));
        panel.add(summaryField);
        return panel;
    }

    @Override
    protected void onConfirm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Project name is required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String summary = summaryField.getText().trim();
        projectController.addProject(name, summary.isEmpty() ? null : summary);
    }
}
