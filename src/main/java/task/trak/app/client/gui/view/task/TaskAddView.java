package task.trak.app.client.gui.view.task;

import task.trak.api.dto.ProjectDTO;
import task.trak.app.client.gui.controller.TaskController;
import task.trak.app.client.gui.view.form.FormDialogView;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class TaskAddView extends FormDialogView {

    private final TaskController taskController;
    private final List<ProjectDTO> projects;
    private final int defaultProjectIndex;

    private JTextField titleField;
    private JComboBox<String> projectCombo;
    private JComboBox<String> assignedCombo;
    private JTextField summaryField;
    private JSpinner deadlineSpinner;
    private JCheckBox deadlineCheck;
    private JTextField estimateField;

    public TaskAddView(Component parent, TaskController taskController, List<ProjectDTO> projects) {
        this(parent, taskController, projects, 0);
    }

    public TaskAddView(Component parent, TaskController taskController, List<ProjectDTO> projects, int defaultProjectIndex) {
        super(parent, "Add Task");
        this.taskController = taskController;
        this.projects = projects;
        this.defaultProjectIndex = defaultProjectIndex;
    }

    @Override
    public void show() {
        if (projects == null || projects.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No projects found. Create a project first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        super.show();
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 4, 4));

        titleField = new JTextField();
        projectCombo = new JComboBox<>();
        for (ProjectDTO p : projects) {
            projectCombo.addItem(p.projectName() + "  (#" + p.id() + ")");
        }
        if (defaultProjectIndex >= 0 && defaultProjectIndex < projects.size()) {
            projectCombo.setSelectedIndex(defaultProjectIndex);
        }

        assignedCombo = new JComboBox<>();
        Runnable updateAssignees = () -> {
            assignedCombo.removeAllItems();
            int idx = projectCombo.getSelectedIndex();
            if (idx >= 0 && idx < projects.size()) {
                ProjectDTO selected = projects.get(idx);
                if (selected.ownerUsername() != null) {
                    assignedCombo.addItem(selected.ownerUsername());
                }
                if (selected.memberUsernames() != null) {
                    for (String member : selected.memberUsernames()) {
                        if (!member.equals(selected.ownerUsername())) {
                            assignedCombo.addItem(member);
                        }
                    }
                }
            }
        };
        updateAssignees.run();
        projectCombo.addActionListener(e -> updateAssignees.run());

        summaryField = new JTextField();

        SpinnerDateModel dateModel = new SpinnerDateModel();
        deadlineSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(deadlineSpinner, "yyyy-MM-dd");
        deadlineSpinner.setEditor(editor);
        deadlineCheck = new JCheckBox("Set deadline");
        deadlineSpinner.setEnabled(false);
        deadlineCheck.addActionListener(e -> deadlineSpinner.setEnabled(deadlineCheck.isSelected()));
        JPanel deadlinePanel = new JPanel(new BorderLayout(4, 0));
        deadlinePanel.add(deadlineCheck, BorderLayout.WEST);
        deadlinePanel.add(deadlineSpinner, BorderLayout.CENTER);

        estimateField = new JTextField();

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Project:"));
        panel.add(projectCombo);
        panel.add(new JLabel("Assigned To:"));
        panel.add(assignedCombo);
        panel.add(new JLabel("Summary:"));
        panel.add(summaryField);
        panel.add(new JLabel("Deadline:"));
        panel.add(deadlinePanel);
        panel.add(new JLabel("Estimate:"));
        panel.add(estimateField);

        return panel;
    }

    @Override
    protected void onConfirm() {
        String title = titleField.getText().trim();
        int selectedIdx = projectCombo.getSelectedIndex();
        if (title.isEmpty() || selectedIdx < 0) {
            JOptionPane.showMessageDialog(parent, "Title and Project are required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String projectName = projects.get(selectedIdx).projectName();
        String assignee = (String) assignedCombo.getSelectedItem();
        String summary = summaryField.getText().trim();
        Date deadline = null;
        if (deadlineCheck.isSelected()) {
            deadline = (Date) deadlineSpinner.getValue();
        }
        String estimate = estimateField.getText().trim();

        taskController.addTask(
                title,
                projectName,
                assignee != null && !assignee.isEmpty() ? assignee : null,
                summary.isEmpty() ? null : summary,
                deadline,
                estimate.isEmpty() ? null : estimate
        );
    }
}
