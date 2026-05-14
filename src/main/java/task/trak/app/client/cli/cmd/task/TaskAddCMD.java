package task.trak.app.client.cli.cmd.task;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.dto.request.CreateTaskRequest;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class TaskAddCMD extends TaskCMD {
    private final TaskService taskService = ServiceFactory.taskService();
    private HashMap<String, String> options;

    public TaskAddCMD(String[] args) {
        super();
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        HashMap<String, String> parsed = new HashMap<>();
        int i = 0;
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
        String projectArg = this.options.get("project");
        if (projectArg == null || projectArg.isEmpty()) {
            throw new IllegalArgumentException("--project is required. Usage: task add --title <title> --project <project_id>");
        }
        Long projectId;
        try {
            projectId = Long.parseLong(projectArg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("--project requires a numeric project ID, got: " + projectArg);
        }
        ProjectService projectService = ServiceFactory.projectService();
        ProjectDTO p = projectService.getById(projectId);
        if (p == null) {
            throw new IllegalArgumentException("Project with ID " + projectId + " not found.");
        }
        String project = p.projectName();

        String title = this.options.get("title");
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("--title is required. Usage: task add --title <title> --project <project_name>");
        }

        String assignedTo = this.options.get("assigned_to");
        String summary = this.options.get("summary");
        String estimate = this.options.get("estimate");

        Date deadline = null;
        String deadlineStr = this.options.get("deadline");
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            deadline = new SimpleDateFormat("yyyy-MM-dd").parse(deadlineStr);
        }

        TaskDTO task = taskService.create(new CreateTaskRequest(title, project, assignedTo, summary, deadline, estimate));
        System.out.println("Task " + task.id() + " created successfully.");
        return Optional.of(task);
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
