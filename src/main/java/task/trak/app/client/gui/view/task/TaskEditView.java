package task.trak.app.client.gui.view.task;

import task.trak.api.dto.TaskDTO;
import task.trak.app.client.gui.controller.TaskController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class TaskEditView extends FormDialogView {

    private final TaskController taskController;
    private final TaskDTO task;
    private final List<String> assignees;

    private JTextField titleField;
    private JComboBox<String> assignedCombo;
    private JComboBox<String> statusCombo;
    private JTextArea summaryArea;
    private TimeInputPanel estimatePanel;

    public TaskEditView(Component parent, TaskController taskController, TaskDTO task, List<String> assignees) {
        super(parent, "Edit Task");
        this.taskController = taskController;
        this.task = task;
        this.assignees = assignees;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();

        JLabel idLabel = new JLabel("#" + task.id() + "  (" + task.projectName() + ")");
        idLabel.setForeground(TrakTheme.TEXT_MUTED);
        form.addField("Task ID:", idLabel);

        titleField = new JTextField(task.title() != null ? task.title() : "", 30);
        form.addField("Title:", titleField);

        assignedCombo = new JComboBox<>();
        if (assignees != null) {
            for (String a : assignees) assignedCombo.addItem(a);
        }
        if (task.assignedTo() != null) assignedCombo.setSelectedItem(task.assignedTo());
        TrakTheme.styleComboBox(assignedCombo);
        form.addField("Assigned To:", assignedCombo);

        statusCombo = new JComboBox<>(new String[]{"READY", "INPROGRESS", "COMPLETE"});
        statusCombo.setSelectedItem(task.status() != null ? task.status() : "READY");
        TrakTheme.styleStatusComboBox(statusCombo);
        form.addField("Status:", statusCombo);

        estimatePanel = new TimeInputPanel();
        if (task.estimate() != null) estimatePanel.setDuration(task.estimate());
        form.addField("Estimate:", estimatePanel);

        summaryArea = new JTextArea(task.summary() != null ? task.summary() : "", 8, 40);
        summaryArea.setFont(TrakTheme.FONT_BODY);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        form.addExpandingField("Summary:", new JScrollPane(summaryArea));

        form.setPreferredSize(new Dimension(520, 420));
        return form;
    }

    @Override
    protected void onConfirm() {
        String newTitle = titleField.getText().trim();
        String newAssigned = (String) assignedCombo.getSelectedItem();
        String newStatus = (String) statusCombo.getSelectedItem();
        String newSummary = summaryArea.getText().trim();
        String newEstimate = estimatePanel.isZero() ? null : estimatePanel.getDurationString();

        String titleArg = diffOrNull(newTitle, task.title());
        String assigneeArg = diffOrNull(newAssigned, task.assignedTo());
        String statusArg = diffOrNull(newStatus, task.status());
        String summaryArg = diffOrNull(newSummary, task.summary());
        String estimateArg = Objects.equals(newEstimate, task.estimate()) ? null : newEstimate;

        if (titleArg != null || assigneeArg != null || statusArg != null || summaryArg != null || estimateArg != null) {
            taskController.updateTask(task.id(), titleArg, statusArg, assigneeArg, summaryArg, estimateArg);
        }
    }

    private String diffOrNull(String newVal, String oldVal) {
        String old = oldVal != null ? oldVal : "";
        return newVal != null && !newVal.equals(old) ? newVal : null;
    }
}
