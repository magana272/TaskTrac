package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;
import task.trak.api.service.TaskService;
import task.trak.model.util.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TasksListCMD extends WorkspaceCMD {
    private final TaskService taskService = ServiceFactory.taskService();
    private final SprintService sprintService = ServiceFactory.sprintService();

    public TasksListCMD(String[] args) {
        super();
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        List<TaskDTO> tasks = taskService.listByAssignee(getCurrentUsername());
        if (tasks.isEmpty()) {
            System.out.println("No tasks assigned to you.");
            return Optional.of(List.of());
        }

        // Build task-to-sprint mapping
        Map<Long, String> taskSprint = new HashMap<>();
        for (SprintDTO s : sprintService.listAll()) {
            if (s.taskIds() != null) {
                for (Long tid : s.taskIds()) {
                    taskSprint.put(tid, s.name());
                }
            }
        }

        System.out.printf("%-16s %-14s %-12s %-10s %-20s %-22s %-12s%n",
                "ID", "Project", "Sprint", "Status", "Task Name", "Summary", "Deadline");
        System.out.println("-".repeat(106));
        for (TaskDTO t : tasks) {
            String sprint = taskSprint.getOrDefault(t.id(), "-");
            String project = t.projectName() != null ? t.projectName() : "-";
            String summary = t.summary() != null ? t.summary() : "";
            if (summary.length() > 20) summary = summary.substring(0, 17) + "...";
            String deadline = t.deadline() != null ? TimeUtil.formatDeadlineRemaining(t.deadline()) : "-";
            System.out.printf("%-16d %-14s %-12s %-10s %-20s %-22s %-12s%n",
                    t.id(),
                    project,
                    sprint,
                    t.status(),
                    t.title(),
                    summary,
                    deadline);
        }
        return Optional.of(tasks);
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
