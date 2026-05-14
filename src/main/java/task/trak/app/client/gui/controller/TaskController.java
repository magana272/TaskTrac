package task.trak.app.client.gui.controller;

import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.CreateTaskRequest;
import task.trak.model.dto.request.UpdateTaskRequest;
import task.trak.model.Session;
import task.trak.app.client.http.TaskHttpService;
import task.trak.app.client.gui.viewmodel.TaskViewModel;
import task.trak.app.client.gui.viewmodel.UserViewModel;

import java.util.Date;
import java.util.List;

public class TaskController {

    private final TaskHttpService taskService;
    private final TaskViewModel taskViewModel;
    private final UserViewModel userViewModel;

    public TaskController(TaskHttpService taskService, TaskViewModel taskViewModel, UserViewModel userViewModel) {
        this.taskService = taskService;
        this.taskViewModel = taskViewModel;
        this.userViewModel = userViewModel;
    }

    public TaskViewModel getViewModel() {
        return taskViewModel;
    }

    public void addTask(String title, String projectName, String assignee, String summary, Date deadline, String estimate) {
        this.taskService.create(new CreateTaskRequest(title, projectName, assignee, summary, deadline, estimate));
        refreshTasks();
    }

    public void updateTask(long id, String title, String status, String assignee, String summary, String estimate) {
        this.taskService.updateById(new UpdateTaskRequest(id, title, status, assignee, summary, estimate));
        refreshTasks();
    }

    public void deleteTask(long id) {
        this.taskService.deleteById(id);
        refreshTasks();
    }

    public void completeTask(long id) {
        this.taskService.updateById(new UpdateTaskRequest(id, null, "COMPLETE", null, null, null));
        refreshTasks();
    }

    public void refreshTasks() {
        refreshTasks(false);
    }

    public void refreshTasks(boolean teamMode) {
        Session session = userViewModel.getSession();
        List<TaskDTO> tasks;
        if (session != null && session.getLogged_in_user() != null) {
            tasks = teamMode
                    ? this.taskService.listAll()
                    : this.taskService.listByAssignee(session.getLogged_in_user());
        } else {
            tasks = List.of();
        }
        taskViewModel.setAll(tasks);
    }

    public TaskDTO getTaskById(long id) {
        return this.taskService.getById(id);
    }
}
