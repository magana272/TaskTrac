package task.trak.app.client.gui.controller;

import task.trak.api.dto.TaskDTO;
import task.trak.api.dto.request.CreateTaskRequest;
import task.trak.api.dto.request.UpdateTaskRequest;
import task.trak.api.model.Session;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.gui.viewmodel.TaskViewModel;
import task.trak.app.client.gui.viewmodel.UserViewModel;

import java.util.Date;
import java.util.List;

public class TaskController {

    private final TaskViewModel taskViewModel;
    private final UserViewModel userViewModel;

    public TaskController(TaskViewModel taskViewModel, UserViewModel userViewModel) {
        this.taskViewModel = taskViewModel;
        this.userViewModel = userViewModel;
    }

    public TaskViewModel getViewModel() {
        return taskViewModel;
    }

    public void addTask(String title, String projectName, String assignee, String summary, Date deadline, String estimate) {
        ServiceFactory.taskService().create(new CreateTaskRequest(title, projectName, assignee, summary, deadline, estimate));
        refreshTasks();
    }

    public void updateTask(long id, String title, String status, String assignee, String summary, String estimate) {
        ServiceFactory.taskService().updateById(new UpdateTaskRequest(id, title, status, assignee, summary, estimate));
        refreshTasks();
    }

    public void deleteTask(long id) {
        ServiceFactory.taskService().deleteById(id);
        refreshTasks();
    }

    public void completeTask(long id) {
        ServiceFactory.taskService().updateById(new UpdateTaskRequest(id, null, "COMPLETE", null, null, null));
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
                    ? ServiceFactory.taskService().listAll()
                    : ServiceFactory.taskService().listByAssignee(session.getLogged_in_user());
        } else {
            tasks = List.of();
        }
        taskViewModel.setAll(tasks);
    }

    public TaskDTO getTaskById(long id) {
        return ServiceFactory.taskService().getById(id);
    }
}
