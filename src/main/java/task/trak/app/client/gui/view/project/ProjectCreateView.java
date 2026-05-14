package task.trak.app.client.gui.view.project;

import task.trak.app.client.gui.controller.ProjectController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

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
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        nameField = new JTextField();
        summaryField = new JTextField();
        form.addField("Project Name:", nameField);
        form.addField("Summary:", summaryField);
        return form;
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
