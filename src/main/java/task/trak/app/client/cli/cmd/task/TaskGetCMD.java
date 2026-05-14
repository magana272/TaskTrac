package task.trak.app.client.cli.cmd.task;

import task.trak.model.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.util.Optional;

public class TaskGetCMD extends TaskCMD {
    private final TaskService taskService = ServiceFactory.taskService();
    private final Long taskId;

    public TaskGetCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Task ID is required for get command");
        }
        this.taskId = Long.parseLong(args[0]);
    }

    @Override
    public Optional<TaskDTO> Execute() throws Exception {
        TaskDTO task = taskService.getById(this.taskId);
        if (task != null) {
            System.out.println("Task: " + task.id());
            System.out.println("Title: " + task.title());
            System.out.println("Project: " + task.projectName());
            System.out.println("Status: " + task.status());
            if (task.assignedTo() != null) {
                System.out.println("Assigned to: " + task.assignedTo());
            }
            if (task.summary() != null) {
                System.out.println("Summary: " + task.summary());
            }
            return Optional.of(task);
        } else {
            System.out.println("Task " + this.taskId + " not found.");
            return Optional.empty();
        }
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
