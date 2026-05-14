package task.trak.app.client.cli.cmd.backlog;

import task.trak.model.dto.BacklogDTO;
import task.trak.api.service.BacklogService;
import task.trak.api.service.ServiceFactory;

import java.util.HashMap;
import java.util.Optional;

public class BacklogUpdateCMD extends BacklogCMD {
    private final BacklogService backlogService = ServiceFactory.backlogService();
    private String backlogName;
    private HashMap<String, String> options;

    public BacklogUpdateCMD(String[] args) {
        super();
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: backlog update <name> --add_task <id> | --remove_task <id>");
        }
        this.backlogName = args[0];
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
    public Optional<BacklogDTO> Execute() throws Exception {
        String addTaskId = this.options.get("add_task");
        String removeTaskId = this.options.get("remove_task");

        BacklogDTO updated = null;
        if (addTaskId != null) {
            updated = backlogService.addTask(this.backlogName, Long.parseLong(addTaskId));
            System.out.println("Task " + addTaskId + " added to backlog \"" + this.backlogName + "\".");
        }
        if (removeTaskId != null) {
            updated = backlogService.removeTask(this.backlogName, Long.parseLong(removeTaskId));
            System.out.println("Task " + removeTaskId + " removed from backlog \"" + this.backlogName + "\".");
        }

        return updated != null ? Optional.of(updated) : Optional.empty();
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
