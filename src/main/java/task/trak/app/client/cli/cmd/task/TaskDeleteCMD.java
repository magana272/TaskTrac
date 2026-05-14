package task.trak.app.client.cli.cmd.task;

import task.trak.model.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.util.Optional;
import java.util.Scanner;

public class TaskDeleteCMD extends TaskCMD {
    private final TaskService taskService = ServiceFactory.taskService();
    private final Long taskId;

    public TaskDeleteCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Task ID is required for delete command");
        }
        this.taskId = Long.parseLong(args[0]);
    }

    @Override
    public Optional<TaskDTO> Execute() throws Exception {
        System.out.println("Confirm deletion of task " + this.taskId + "? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            boolean deleted = taskService.deleteById(this.taskId);
            if (deleted) {
                System.out.println("Task " + this.taskId + " deleted successfully.");
            } else {
                System.out.println("Task " + this.taskId + " not found.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
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
