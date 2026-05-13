package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;

import java.util.Optional;

public class AddMemberCMD extends WorkspaceCMD {
    private final String projectName;
    private final String username;
    private final ProjectService projectService = ServiceFactory.projectService();

    public AddMemberCMD(String[] args) {
        super();
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Usage: addmember <project_name> <username>");
        }
        this.projectName = args[0];
        this.username = args[1];
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        ProjectDTO project = projectService.addMember(this.projectName, this.username);
        System.out.println("User \"" + this.username + "\" added to project \"" + this.projectName + "\".");
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
