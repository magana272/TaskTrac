package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;

import java.util.List;
import java.util.Optional;

public class ProjectsListCMD extends WorkspaceCMD {
    private final ProjectService projectService = ServiceFactory.projectService();

    public ProjectsListCMD(String[] args) {
        super();
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        List<ProjectDTO> projects = projectService.listByUser(getCurrentUsername());
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return Optional.of(List.of());
        }
        System.out.printf("%-16s %-20s %-30s %-16s%n",
                "Project ID", "Project Name", "Description", "Contact");
        System.out.println("-".repeat(82));
        for (ProjectDTO p : projects) {
            String description = p.summary() != null ? p.summary() : "-";
            if (description.length() > 28) description = description.substring(0, 25) + "...";
            String contact = p.ownerUsername() != null ? p.ownerUsername() : "-";
            System.out.printf("%-16d %-20s %-30s %-16s%n",
                    p.id(), p.projectName(), description, contact);
        }
        return Optional.of(projects);
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
