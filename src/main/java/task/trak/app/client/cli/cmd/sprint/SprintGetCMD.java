package task.trak.app.client.cli.cmd.sprint;

import task.trak.api.dto.SprintDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;

import java.util.Optional;

public class SprintGetCMD extends SprintCMD {
    private final SprintService sprintService = ServiceFactory.sprintService();
    private final Long sprintId;

    public SprintGetCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Sprint ID is required for get command");
        }
        try {
            this.sprintId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Sprint get requires a numeric ID, got: " + args[0]);
        }
    }

    @Override
    public Optional<SprintDTO> Execute() throws Exception {
        SprintDTO sprint = sprintService.getById(this.sprintId);
        if (sprint != null) {
            System.out.println("Sprint: " + sprint.name());
            System.out.println("Project: " + sprint.projectName());
            if (sprint.startDate() != null) {
                System.out.println("Start: " + sprint.startDate());
            }
            if (sprint.endDate() != null) {
                System.out.println("End: " + sprint.endDate());
            }
            System.out.println("Tasks: " + sprint.taskIds().size());
            return Optional.of(sprint);
        } else {
            System.out.println("Sprint with ID " + this.sprintId + " not found.");
            return Optional.empty();
        }
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
