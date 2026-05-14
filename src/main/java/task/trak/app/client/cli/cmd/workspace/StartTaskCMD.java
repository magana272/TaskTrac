package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.util.Optional;
import java.util.Scanner;

public class StartTaskCMD extends WorkspaceCMD {
    private final Long taskId;
    private final TaskService taskService = ServiceFactory.taskService();

    public StartTaskCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: start <task_id>");
        }
        this.taskId = Long.parseLong(args[0]);
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        TaskDTO taskDTO = taskService.getById(this.taskId);
        if (taskDTO == null) {
            System.out.println("Task " + this.taskId + " not found.");
            return Optional.empty();
        }
        if (!getCurrentUsername().equals(taskDTO.assignedTo())) {
            System.out.println("Task " + this.taskId + " is not assigned to you.");
            return Optional.empty();
        }

        // Check if already working on a task
        Long currentTaskId = this.session.getCurrent_task_id();
        if (currentTaskId != null) {
            TaskDTO currentTaskDTO = taskService.getById(currentTaskId);
            String currentTitle = currentTaskDTO != null ? currentTaskDTO.title() : String.valueOf(currentTaskId);
            System.out.println("You are currently working on: " + currentTitle + ". Swap? (yes/no)");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();
            if (!"yes".equals(response)) {
                System.out.println("Keeping current task.");
                return Optional.empty();
            }
            // Stop the current task via service
            taskService.updateById(currentTaskId, null, "READY", null, null, null);
        }

        // Start the new task via service
        taskService.updateById(this.taskId, null, "INPROGRESS", null, null, null);

        long now = System.currentTimeMillis();
        this.session.setCurrent_task_id(this.taskId);
        this.session.setTask_started_at(now);
        System.out.println("Started: " + taskDTO.title());
        return Optional.empty();
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
