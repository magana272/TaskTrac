package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.CreateSprintRequest;
import task.trak.model.dto.request.UpdateSprintRequest;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;
import task.trak.api.service.TaskService;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SprintPlanCMD extends WorkspaceCMD {
    private final ProjectService projectService = ServiceFactory.projectService();
    private final SprintService sprintService = ServiceFactory.sprintService();
    private final TaskService taskService = ServiceFactory.taskService();

    public SprintPlanCMD(String[] args) {
        super();
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        Scanner scanner = new Scanner(System.in);

        // 1. List projects where user is owner
        List<ProjectDTO> owned = projectService.listByUser(getCurrentUsername()).stream()
                .filter(p -> p.ownerUsername() != null && getCurrentUsername().equals(p.ownerUsername()))
                .collect(Collectors.toList());

        if (owned.isEmpty()) {
            System.out.println("You don't own any projects.");
            return Optional.empty();
        }

        System.out.println("Your projects:");
        for (int i = 0; i < owned.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + owned.get(i).projectName());
        }
        System.out.print("Select project (number): ");
        int projectIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (projectIdx < 0 || projectIdx >= owned.size()) {
            System.out.println("Invalid selection.");
            return Optional.empty();
        }
        ProjectDTO project = owned.get(projectIdx);

        // 2. Sprint info
        System.out.print("Sprint name: ");
        String sprintName = scanner.nextLine().trim();
        if (sprintName.isEmpty()) {
            System.out.println("Sprint name is required.");
            return Optional.empty();
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        fmt.setLenient(false);
        Date today = fmt.parse(fmt.format(new Date()));

        System.out.print("Start date (yyyy-MM-dd): ");
        String startDate = scanner.nextLine().trim();
        Date startParsed = null;
        if (!startDate.isEmpty()) {
            try {
                startParsed = fmt.parse(startDate);
            } catch (Exception e) {
                System.out.println("Invalid date format. Expected yyyy-MM-dd.");
                return Optional.empty();
            }
            if (startParsed.before(today)) {
                System.out.println("Start date must be today or later.");
                return Optional.empty();
            }
        }

        System.out.print("End date (yyyy-MM-dd): ");
        String endDate = scanner.nextLine().trim();
        if (!endDate.isEmpty() && startParsed != null) {
            Date endParsed;
            try {
                endParsed = fmt.parse(endDate);
            } catch (Exception e) {
                System.out.println("Invalid date format. Expected yyyy-MM-dd.");
                return Optional.empty();
            }
            if (!endParsed.after(startParsed)) {
                System.out.println("End date must be after start date.");
                return Optional.empty();
            }
            Calendar maxEnd = Calendar.getInstance();
            maxEnd.setTime(startParsed);
            maxEnd.add(Calendar.YEAR, 1);
            if (endParsed.after(maxEnd.getTime())) {
                System.out.println("End date must be within 1 year of start date.");
                return Optional.empty();
            }
        }

        // 3. Create sprint
        SprintDTO sprint = sprintService.create(new CreateSprintRequest(sprintName, project.projectName()));

        // 4. List tasks for this project
        List<TaskDTO> projectTasks = taskService.listAll().stream()
                .filter(t -> project.projectName().equals(t.projectName()))
                .collect(Collectors.toList());

        List<Long> selectedIds = null;
        if (!projectTasks.isEmpty()) {
            System.out.println("\nTasks in " + project.projectName() + ":");
            for (int i = 0; i < projectTasks.size(); i++) {
                TaskDTO t = projectTasks.get(i);
                System.out.println("  " + (i + 1) + ". [" + t.status() + "] " + t.title() + " (" + t.id() + ")");
            }
            System.out.print("Select tasks (e.g. 1,2,3 or blank for none): ");
            String selection = scanner.nextLine().trim();

            if (!selection.isEmpty()) {
                selectedIds = new ArrayList<>();
                String[] parts = selection.split(",");
                for (String part : parts) {
                    try {
                        int idx = Integer.parseInt(part.trim()) - 1;
                        if (idx >= 0 && idx < projectTasks.size()) {
                            selectedIds.add(projectTasks.get(idx).id());
                        } else {
                            System.out.println("Skipping invalid selection: " + part.trim());
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid input: " + part.trim());
                    }
                }
                if (selectedIds.isEmpty()) selectedIds = null;
            }
        } else {
            System.out.println("No tasks found for project \"" + project.projectName() + "\".");
        }

        if (!startDate.isEmpty() || !endDate.isEmpty() || selectedIds != null) {
            sprintService.update(new UpdateSprintRequest(
                    sprintName, project.projectName(),
                    startDate.isEmpty() ? null : startDate,
                    endDate.isEmpty() ? null : endDate,
                    selectedIds, null));
        } else if (projectTasks.isEmpty()) {
            System.out.println("Sprint \"" + sprintName + "\" created (empty).");
            return Optional.empty();
        }

        System.out.println("Sprint \"" + sprintName + "\" planned for project \"" + project.projectName() + "\".");
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
