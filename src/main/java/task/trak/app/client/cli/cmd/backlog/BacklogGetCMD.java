package task.trak.app.client.cli.cmd.backlog;

import task.trak.model.dto.BacklogDTO;
import task.trak.api.service.BacklogService;
import task.trak.api.service.ServiceFactory;

import java.util.Optional;

public class BacklogGetCMD extends BacklogCMD {
    private final BacklogService backlogService = ServiceFactory.backlogService();
    private final String backlogName;

    public BacklogGetCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Backlog name is required for get command");
        }
        this.backlogName = args[0];
    }

    @Override
    public Optional<BacklogDTO> Execute() throws Exception {
        BacklogDTO backlog = backlogService.getByName(this.backlogName);
        if (backlog != null) {
            System.out.println("Backlog: " + backlog.name());
            System.out.println("Project: " + backlog.projectName());
            System.out.println("Tasks: " + backlog.taskIds().size());
            return Optional.of(backlog);
        } else {
            System.out.println("Backlog \"" + this.backlogName + "\" not found.");
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
