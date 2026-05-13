package task.trak.app.client.cli.cmd.backlog;

import task.trak.api.dto.BacklogDTO;
import task.trak.api.service.BacklogService;
import task.trak.api.service.ServiceFactory;

import java.util.HashMap;
import java.util.Optional;

public class BacklogAddCMD extends BacklogCMD {
    private final BacklogService backlogService = ServiceFactory.backlogService();
    private final String backlogName;
    private HashMap<String, String> options;

    public BacklogAddCMD(String[] args) {
        super();
        this.backlogName = args[0];
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        HashMap<String, String> parsed = new HashMap<>();
        parsed.put("name", args[0]);
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
        String project = this.options.get("project");
        if (project == null || project.isEmpty()) {
            throw new IllegalArgumentException("--project is required. Usage: backlog add <name> --project <project_name>");
        }

        BacklogDTO backlog = backlogService.create(this.backlogName, project);
        System.out.println("Backlog \"" + this.backlogName + "\" created successfully.");
        return Optional.of(backlog);
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
