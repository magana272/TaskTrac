package task.trak.app.client.cli.cmd.project;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;

import java.util.Optional;
import java.util.Scanner;

public class ProjectDeleteCMD extends ProjectCMD {
    private final ProjectService projectService = ServiceFactory.projectService();
    private String projectName;

    public ProjectDeleteCMD(String[] options) {
        super();
        this.parse(options);
    }

    public void parse(String[] options) {
        if (options == null || options.length == 0) {
            throw new IllegalArgumentException("Project name is required for delete command");
        }
        this.projectName = options[0];
    }

    @Override
    public Optional<ProjectDTO> Execute() throws Exception {
        System.out.println("Confirm deletion of project \"" + this.projectName + "\"? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            boolean deleted = projectService.deleteByName(this.projectName);
            if (deleted) {
                System.out.println("Project \"" + this.projectName + "\" deleted successfully.");
            } else {
                System.out.println("Project \"" + this.projectName + "\" not found.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        return Optional.empty();
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error deleting project: " + e.getMessage());
        }
    }

}
