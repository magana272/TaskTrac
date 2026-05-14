package task.trak.app.client.cli.cmd.task;

import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.util.HashMap;
import java.util.Optional;

public class TaskUpdateCMD extends TaskCMD {
    private final TaskService taskService = ServiceFactory.taskService();
    private Long taskId;
    private HashMap<String, String> options;

    public TaskUpdateCMD(String[] args) {
        super();
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: task update <id> --<field> <value>");
        }
        this.taskId = Long.parseLong(args[0]);
        HashMap<String, String> parsed = new HashMap<>();
        int i = 1;
        while (i < args.length) {
            if (args[i].startsWith("--")) {
                String key = args[i].replaceFirst("^--", "");
                StringBuilder value = new StringBuilder();
                i++;
                while (i < args.length && !args[i].startsWith("--")) {
                    if (value.length() > 0) value.append(" ");
                    value.append(args[i]);
                    i++;
                }
                parsed.put(key, value.toString());
            } else {
                i++;
            }
        }
        this.options = parsed;
        return parsed;
    }

    @Override
    public Optional<TaskDTO> Execute() throws Exception {
        String newTitle = this.options.get("title");
        String newStatus = this.options.get("status");
        String newAssignedTo = this.options.get("assigned_to");
        String newSummary = this.options.get("summary");

        TaskDTO updated = taskService.updateById(this.taskId, newTitle, newStatus, newAssignedTo, newSummary, null);
        System.out.println("Task " + this.taskId + " updated successfully.");
        return Optional.of(updated);
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
