package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;
import task.trak.api.service.TaskService;
import task.trak.model.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class DetailCMD extends WorkspaceCMD {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private final ProjectService projectService = ServiceFactory.projectService();
    private final TaskService taskService = ServiceFactory.taskService();
    private final SprintService sprintService = ServiceFactory.sprintService();
    private final String entityType;  // "sprint", "project", "task", or null (legacy)
    private final String entityValue;

    public DetailCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: detail [-s <sprint_id> | -p <project_id> | -t <task_id> | <name_or_id>]");
        }

        if (args[0].equals("-s") && args.length >= 2) {
            this.entityType = "sprint";
            this.entityValue = args[1];
        } else if (args[0].equals("-p") && args.length >= 2) {
            this.entityType = "project";
            this.entityValue = args[1];
        } else if (args[0].equals("-t") && args.length >= 2) {
            this.entityType = "task";
            this.entityValue = args[1];
        } else {
            this.entityType = null;
            this.entityValue = args[0];
        }
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        if (entityType != null) {
            Long id = parseId(entityValue, entityType);
            switch (entityType) {
                case "sprint" -> {
                    SprintDTO sprint = sprintService.getById(id);
                    if (sprint != null) {
                        printSprint(sprint);
                        return Optional.of(sprint);
                    } else {
                        System.out.println("Not found: sprint " + id);
                    }
                }
                case "project" -> {
                    ProjectDTO project = projectService.getById(id);
                    if (project != null) {
                        printProject(project);
                        return Optional.of(project);
                    } else {
                        System.out.println("Not found: project " + id);
                    }
                }
                case "task" -> {
                    TaskDTO task = taskService.getById(id);
                    if (task != null) {
                        printTask(task);
                        return Optional.of(task);
                    } else {
                        System.out.println("Not found: task " + id);
                    }
                }
            }
            return Optional.empty();
        }

        // Legacy mode: guess type
        ProjectDTO project = projectService.getByName(this.entityValue);
        if (project != null) {
            printProject(project);
            return Optional.of(project);
        }

        SprintDTO sprint = sprintService.getByName(this.entityValue);
        if (sprint != null) {
            printSprint(sprint);
            return Optional.of(sprint);
        }

        try {
            Long taskId = Long.parseLong(this.entityValue);
            TaskDTO task = taskService.getById(taskId);
            if (task != null) {
                printTask(task);
                return Optional.of(task);
            }
        } catch (NumberFormatException ignored) {
        }

        System.out.println("Not found: " + this.entityValue);
        return Optional.empty();
    }

    private Long parseId(String value, String type) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + type + " ID: " + value);
        }
    }

    private void printProject(ProjectDTO p) {
        System.out.println("Project: " + p.projectName());
        if (p.summary() != null) System.out.println("Summary: " + p.summary());
        if (p.ownerUsername() != null) System.out.println("Owner: " + p.ownerUsername());
        System.out.println("Members: " + p.memberCount());
        List<TaskDTO> tasks = taskService.listAll().stream()
                .filter(t -> p.projectName().equals(t.projectName()))
                .toList();
        System.out.println("Tasks: " + tasks.size());
        System.out.println("Sprints: " + p.sprintCount());
    }

    private void printSprint(SprintDTO s) {
        String start = s.startDate() != null ? DATE_FMT.format(s.startDate()) : "not set";
        String end = s.endDate() != null ? DATE_FMT.format(s.endDate()) : "not set";

        System.out.printf("%-15s %-20s %-12s %-12s%n", "ID", "Sprint Name", "Start", "End");
        System.out.println("-".repeat(59));
        System.out.printf("%-15d %-20s %-12s %-12s%n", s.id(), s.name(), start, end);
        System.out.println();

        int total = s.taskIds() != null ? s.taskIds().size() : 0;
        int completed = 0;
        if (s.taskIds() != null) {
            for (Long taskId : s.taskIds()) {
                TaskDTO t = taskService.getById(taskId);
                if (t != null && "COMPLETE".equals(t.status())) {
                    completed++;
                }
            }
        }
        System.out.println("Completed Tasks: " + completed);
        System.out.println("Total Tasks: " + total);
    }

    private void printTask(TaskDTO t) {
        System.out.println("Task: " + t.id());
        System.out.println("Title: " + t.title());
        System.out.println("Project: " + t.projectName());
        System.out.println("Status: " + t.status());
        if (t.assignedTo() != null) System.out.println("Assigned to: " + t.assignedTo());
        if (t.summary() != null) System.out.println("Summary: " + t.summary());
        if (t.deadline() != null) System.out.println("Deadline: " + TimeUtil.formatDeadlineRemaining(t.deadline()));
        if (t.estimate() != null) System.out.println("Estimate: " + t.estimate());
        if (t.timeSpentMs() > 0) System.out.println("Time spent: " + TimeUtil.formatDuration(t.timeSpentMs()));
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
