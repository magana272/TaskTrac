package task.trak.app.client.gui.view;

import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.project.ProjectSelectorPanel;
import task.trak.app.client.gui.view.sprint.SprintProgressPanel;
import task.trak.app.client.gui.view.task.TasksView;
import task.trak.app.client.gui.viewmodel.*;

import javax.swing.*;
import java.awt.*;

public class DashboardView extends JPanel implements ViewModelChangeListener {

    private final GUIController controller;
    private final ProjectSelectorPanel projectSelector;
    private final TasksView tasksView;
    private final SprintProgressPanel sprintProgress;
    private final ProjectViewModel projectViewModel;
    private final TaskViewModel taskViewModel;
    private final SprintViewModel sprintViewModel;

    public DashboardView(GUIController controller,
                         TaskViewModel taskViewModel,
                         ProjectViewModel projectViewModel,
                         SprintViewModel sprintViewModel) {
        this.controller = controller;
        this.taskViewModel = taskViewModel;
        this.projectViewModel = projectViewModel;
        this.sprintViewModel = sprintViewModel;

        setLayout(new BorderLayout());
        setBackground(TrakTheme.BG_DARK);

        projectSelector = new ProjectSelectorPanel(controller);
        tasksView = new TasksView(controller);
        sprintProgress = new SprintProgressPanel(controller);

        JScrollPane taskScroll = new JScrollPane(tasksView);
        taskScroll.setBorder(BorderFactory.createEmptyBorder());
        taskScroll.getViewport().setBackground(TrakTheme.BG_DARK);

        add(projectSelector, BorderLayout.NORTH);
        add(taskScroll, BorderLayout.CENTER);
        add(sprintProgress, BorderLayout.SOUTH);

        taskViewModel.addObserver(this);
        projectViewModel.addObserver(this);
        sprintViewModel.addObserver(this);
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case PROJECTS -> {
                    projectSelector.refresh(projectViewModel.get());
                    // Also sync task filter with selected project
                    String selected = projectViewModel.getSelectedProject();
                    taskViewModel.setProjectFilter(selected);
                }
                case TASKS -> {
                    tasksView.render();
                    sprintProgress.refresh(
                            sprintViewModel.get(),
                            taskViewModel.get(),
                            projectViewModel.getSelectedProject());
                }
                case SPRINTS -> {
                    sprintProgress.refresh(
                            sprintViewModel.get(),
                            taskViewModel.get(),
                            projectViewModel.getSelectedProject());
                }
            }
        });
    }

    public void render() {
        projectSelector.refresh(projectViewModel.get());
        tasksView.render();
        sprintProgress.refresh(
                sprintViewModel.get(),
                taskViewModel.get(),
                projectViewModel.getSelectedProject());
    }

    public TasksView getTasksView() { return tasksView; }
}
