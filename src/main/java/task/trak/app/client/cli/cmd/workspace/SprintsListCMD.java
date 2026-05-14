package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;
import task.trak.api.service.TaskService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SprintsListCMD extends WorkspaceCMD {
    private final SprintService sprintService = ServiceFactory.sprintService();
    private final TaskService taskService = ServiceFactory.taskService();

    public SprintsListCMD(String[] args) {
        super();
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        // Find task IDs assigned to me
        Set<Long> myTaskIds = taskService.listByAssignee(getCurrentUsername()).stream()
                .map(TaskDTO::id)
                .collect(Collectors.toSet());

        // Find sprints containing any of my tasks
        List<SprintDTO> mySprints = sprintService.listAll().stream()
                .filter(s -> s.taskIds() != null && s.taskIds().stream().anyMatch(myTaskIds::contains))
                .collect(Collectors.toList());

        if (mySprints.isEmpty()) {
            System.out.println("No sprints found with your tasks.");
            return Optional.of(List.of());
        }

        System.out.printf("%-20s %-20s %-12s%n", "Sprint Name", "Project", "Sprint ID");
        System.out.println("-".repeat(52));
        for (SprintDTO s : mySprints) {
            System.out.printf("%-20s %-20s %-12d%n",
                    s.name(),
                    s.projectName() != null ? s.projectName() : "",
                    s.id());
        }
        return Optional.of(mySprints);
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
