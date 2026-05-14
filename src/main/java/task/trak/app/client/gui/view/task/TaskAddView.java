package task.trak.app.client.gui.view.task;

import task.trak.api.dto.ProjectDTO;
import task.trak.app.client.gui.controller.TaskController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

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
    private TimeInputPanel estimatePanel;

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
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();

        titleField = new JTextField();
        form.addField("Title:", titleField);

        projectCombo = new JComboBox<>();
        for (ProjectDTO p : projects) {
            projectCombo.addItem(p.projectName() + "  (#" + p.id() + ")");
        }
        if (defaultProjectIndex >= 0 && defaultProjectIndex < projects.size()) {
            projectCombo.setSelectedIndex(defaultProjectIndex);
        }
        TrakTheme.styleComboBox(projectCombo);
        form.addField("Project:", projectCombo);

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
        TrakTheme.styleComboBox(assignedCombo);
        form.addField("Assigned To:", assignedCombo);

        summaryField = new JTextField();
        form.addField("Summary:", summaryField);

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
        form.addField("Deadline:", deadlinePanel);

        estimatePanel = new TimeInputPanel();
        form.addField("Estimate:", estimatePanel);

        return form;
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
        String estimate = estimatePanel.isZero() ? null : estimatePanel.getDurationString();

        taskController.addTask(
                title,
                projectName,
                assignee != null && !assignee.isEmpty() ? assignee : null,
                summary.isEmpty() ? null : summary,
                deadline,
                estimate
        );
    }
}
