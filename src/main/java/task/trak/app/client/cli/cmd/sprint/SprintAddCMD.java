package task.trak.app.client.cli.cmd.sprint;

import task.trak.api.dto.SprintDTO;
import task.trak.api.dto.request.CreateSprintRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;

import java.util.HashMap;
import java.util.Optional;

public class SprintAddCMD extends SprintCMD {
    private final SprintService sprintService = ServiceFactory.sprintService();
    private final String sprintName;
    private HashMap<String, String> options;

    public SprintAddCMD(String[] args) {
        super();
        this.sprintName = args[0];
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
    public Optional<SprintDTO> Execute() throws Exception {
        String project = this.options.get("project");
        if (project == null || project.isEmpty()) {
            throw new IllegalArgumentException("--project is required. Usage: sprint add <name> --project <project_name>");
        }

        SprintDTO sprint = sprintService.create(new CreateSprintRequest(this.sprintName, project));
        System.out.println("Sprint " + sprint.id() + " \"" + this.sprintName + "\" created successfully.");
        return Optional.of(sprint);
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
