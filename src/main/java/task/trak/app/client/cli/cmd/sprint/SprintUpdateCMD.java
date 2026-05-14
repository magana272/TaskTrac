package task.trak.app.client.cli.cmd.sprint;

import task.trak.api.dto.SprintDTO;
import task.trak.api.dto.request.UpdateSprintRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SprintUpdateCMD extends SprintCMD {
    private final SprintService sprintService = ServiceFactory.sprintService();
    private String sprintKey;
    private HashMap<String, String> options;

    public SprintUpdateCMD(String[] args) {
        super();
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: sprint update <name or id> --<field> <value>");
        }
        this.sprintKey = args[0];
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

    private boolean isNumeric(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private SprintDTO resolveSprint() {
        if (isNumeric(this.sprintKey)) {
            return sprintService.getById(Long.parseLong(this.sprintKey));
        }
        String projectName = this.options.get("project");
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sprint name is ambiguous. Use --project <project> or pass the sprint ID.");
        }
        return sprintService.getByNameAndProject(this.sprintKey, projectName);
    }

    @Override
    public Optional<SprintDTO> Execute() throws Exception {
        SprintDTO sprint = resolveSprint();
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint \"" + this.sprintKey + "\" not found.");
        }

        String newStartDate = this.options.get("start_date");
        String newEndDate = this.options.get("end_date");

        List<Long> taskIds = null;
        String addTaskId = this.options.get("add_task");
        if (addTaskId != null) {
            taskIds = sprint.taskIds() != null ? new ArrayList<>(sprint.taskIds()) : new ArrayList<>();
            taskIds.add(Long.parseLong(addTaskId));
        }

        if (newStartDate != null || newEndDate != null || taskIds != null) {
            sprintService.update(new UpdateSprintRequest(sprint.name(), sprint.projectName(), newStartDate, newEndDate, taskIds));
        }

        SprintDTO updated = sprintService.getById(sprint.id());
        System.out.println("Sprint \"" + (updated != null ? updated.name() : this.sprintKey) + "\" updated successfully.");
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
