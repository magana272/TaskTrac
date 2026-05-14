package task.trak.app.client.gui.view.sprint;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.app.client.gui.controller.SprintController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SprintAddView extends FormDialogView {

    private final SprintController sprintController;
    private final List<ProjectDTO> projects;
    private final List<TaskDTO> tasks;
    private final List<JCheckBox> taskCheckboxes = new ArrayList<>();
    private JTextField nameField;
    private JComboBox<String> projectDropdown;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JPanel taskCheckboxPanel;
    private List<TaskDTO> filteredTasks = new ArrayList<>();

    public SprintAddView(Component parent, SprintController sprintController,
                         List<ProjectDTO> projects, List<TaskDTO> tasks) {
        super(parent, "Add Sprint");
        this.sprintController = sprintController;
        this.projects = projects;
        this.tasks = tasks;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();

        nameField = new JTextField(20);
        form.addField("Sprint Name:", nameField);

        String[] projectNames = projects.stream()
                .map(ProjectDTO::projectName)
                .toArray(String[]::new);
        projectDropdown = new JComboBox<>(projectNames);
        projectDropdown.addActionListener(e -> updateTaskCheckboxes());
        TrakTheme.styleComboBox(projectDropdown);
        form.addField("Project:", projectDropdown);

        SpinnerDateModel startModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        startDateSpinner = new JSpinner(startModel);
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        form.addField("Start Date:", startDateSpinner);

        SpinnerDateModel endModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        endDateSpinner = new JSpinner(endModel);
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        form.addField("End Date:", endDateSpinner);

        taskCheckboxPanel = new JPanel();
        taskCheckboxPanel.setLayout(new BoxLayout(taskCheckboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(taskCheckboxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        form.addExpandingField("Tasks:", scrollPane);

        updateTaskCheckboxes();

        return form;
    }

    private void updateTaskCheckboxes() {
        taskCheckboxPanel.removeAll();
        taskCheckboxes.clear();

        String selectedProject = (String) projectDropdown.getSelectedItem();
        if (selectedProject == null) {
            filteredTasks = new ArrayList<>();
        } else {
            filteredTasks = tasks.stream()
                    .filter(t -> selectedProject.equals(t.projectName()))
                    .toList();
        }

        for (TaskDTO task : filteredTasks) {
            JCheckBox checkBox = new JCheckBox(task.id() + " - " + task.title());
            taskCheckboxes.add(checkBox);
            taskCheckboxPanel.add(checkBox);
        }

        taskCheckboxPanel.revalidate();
        taskCheckboxPanel.repaint();
    }

    @Override
    protected void onConfirm() {
        String name = nameField.getText().trim();
        String selectedProject = (String) projectDropdown.getSelectedItem();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format((Date) startDateSpinner.getValue());
        String endDate = sdf.format((Date) endDateSpinner.getValue());

        List<Long> selectedTaskIds = new ArrayList<>();
        for (int i = 0; i < taskCheckboxes.size(); i++) {
            if (taskCheckboxes.get(i).isSelected()) {
                selectedTaskIds.add(filteredTasks.get(i).id());
            }
        }

        sprintController.addSprint(name, selectedProject, startDate, endDate, selectedTaskIds);
    }
}
