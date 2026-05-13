package task.trak.app.client.gui.view.task;

import task.trak.api.dto.TaskDTO;
import task.trak.app.client.gui.controller.TaskController;
import task.trak.app.client.gui.view.form.FormDialogView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TaskEditView extends FormDialogView {

    private final TaskController taskController;
    private final TaskDTO task;
    private final List<String> assignees;

    private JTextField titleField;
    private JComboBox<String> assignedCombo;
    private JComboBox<String> statusCombo;
    private JTextArea summaryArea;

    public TaskEditView(Component parent, TaskController taskController, TaskDTO task, List<String> assignees) {
        super(parent, "Edit Task");
        this.taskController = taskController;
        this.task = task;
        this.assignees = assignees;
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Task ID (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Task ID:"), gbc);
        JLabel idLabel = new JLabel(String.valueOf(task.id()));
        idLabel.setEnabled(false);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(idLabel, gbc);

        // Title
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Title:"), gbc);
        titleField = new JTextField(task.title() != null ? task.title() : "", 20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(titleField, gbc);

        // Assigned To (dropdown)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Assigned To:"), gbc);
        assignedCombo = new JComboBox<>();
        if (assignees != null) {
            for (String a : assignees) {
                assignedCombo.addItem(a);
            }
        }
        if (task.assignedTo() != null) {
            assignedCombo.setSelectedItem(task.assignedTo());
        }
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(assignedCombo, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("Status:"), gbc);
        statusCombo = new JComboBox<>(new String[]{"READY", "INPROGRESS", "COMPLETE"});
        statusCombo.setSelectedItem(task.status() != null ? task.status() : "READY");
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(statusCombo, gbc);

        // Summary (text area)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        panel.add(new JLabel("Summary:"), gbc);
        summaryArea = new JTextArea(task.summary() != null ? task.summary() : "", 4, 20);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(scrollPane, gbc);

        return panel;
    }

    @Override
    protected void onConfirm() {
        String newTitle = titleField.getText().trim();
        String newAssigned = (String) assignedCombo.getSelectedItem();
        String newStatus = (String) statusCombo.getSelectedItem();
        String newSummary = summaryArea.getText().trim();

        // Only send changed fields
        boolean changed = false;
        String titleArg = null;
        String statusArg = null;
        String assigneeArg = null;
        String summaryArg = null;

        if (!newTitle.equals(task.title() != null ? task.title() : "")) {
            titleArg = newTitle;
            changed = true;
        }
        if (newAssigned != null && !newAssigned.equals(task.assignedTo() != null ? task.assignedTo() : "")) {
            assigneeArg = newAssigned;
            changed = true;
        }
        if (newStatus != null && !newStatus.equals(task.status())) {
            statusArg = newStatus;
            changed = true;
        }
        if (!newSummary.equals(task.summary() != null ? task.summary() : "")) {
            summaryArg = newSummary;
            changed = true;
        }

        if (changed) {
            taskController.updateTask(task.id(), titleArg, statusArg, assigneeArg, summaryArg);
        }
    }
}
