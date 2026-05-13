package task.trak.app.client.cli.cmd.project;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.util.List;
import java.util.Optional;

public class ProjectGetCMD extends ProjectCMD {
    private final ProjectService projectService = ServiceFactory.projectService();
    private final TaskService taskService = ServiceFactory.taskService();
    private final Long projectId;

    public ProjectGetCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Project ID is required for get command");
        }
        try {
            this.projectId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Project get requires a numeric ID, got: " + args[0]);
        }
    }

    @Override
    public Optional<ProjectDTO> Execute() throws Exception {
        ProjectDTO project = projectService.getById(this.projectId);
        if (project != null) {
            System.out.println("Project: " + project.projectName());
            if (project.summary() != null) {
                System.out.println("Summary: " + project.summary());
            }
            if (project.ownerUsername() != null) {
                System.out.println("Owner: " + project.ownerUsername());
            }
            System.out.println("Members: " + project.memberCount());

            List<TaskDTO> tasks = taskService.listAll().stream()
                    .filter(t -> project.projectName().equals(t.projectName()))
                    .toList();
            System.out.println("Tasks: " + tasks.size());

            System.out.println("Sprints: " + project.sprintCount());
            return Optional.of(project);
        } else {
            System.out.println("Project with ID " + this.projectId + " not found.");
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
