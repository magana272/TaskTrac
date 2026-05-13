package task.trak.app.client.gui.controller;

import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.gui.viewmodel.TaskViewModel;

import java.util.Date;
import java.util.List;

public class TaskController {

    private final TaskViewModel taskViewModel;

    public TaskController(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
    }

    public TaskViewModel getViewModel() {
        return taskViewModel;
    }

    public void addTask(String title, String projectName, String assignee, String summary, Date deadline, String estimate) {
        ServiceFactory.taskService().create(title, projectName, assignee, summary, deadline, estimate);
        refreshTasks();
    }

    public void updateTask(long id, String title, String status, String assignee, String summary) {
        ServiceFactory.taskService().updateById(id, title, status, assignee, summary);
        refreshTasks();
    }

    public void deleteTask(long id) {
        ServiceFactory.taskService().deleteById(id);
        refreshTasks();
    }

    public void completeTask(long id) {
        ServiceFactory.taskService().updateById(id, null, "COMPLETE", null, null);
        refreshTasks();
    }

    public void refreshTasks() {
        List<TaskDTO> tasks = ServiceFactory.taskService().listAll();
        taskViewModel.setAll(tasks);
    }

    public TaskDTO getTaskById(long id) {
        return ServiceFactory.taskService().getById(id);
    }
}
