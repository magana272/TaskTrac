package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.dto.request.CreateTaskRequest;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Scanner;

public class AddTaskCMD extends WorkspaceCMD {
    private final String projectName;
    private final ProjectService projectService = ServiceFactory.projectService();
    private final TaskService taskService = ServiceFactory.taskService();

    public AddTaskCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: addtask <project_name>");
        }
        this.projectName = args[0];
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        ProjectDTO project = projectService.getByName(this.projectName);
        if (project == null) {
            System.out.println("Project \"" + this.projectName + "\" not found.");
            return Optional.empty();
        }
        if (project.ownerUsername() == null || !getCurrentUsername().equals(project.ownerUsername())) {
            System.out.println("You must be the owner of project \"" + this.projectName + "\" to add tasks.");
            return Optional.empty();
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title is required.");
            return Optional.empty();
        }

        System.out.print("Summary: ");
        String summary = scanner.nextLine().trim();
        if (summary.isEmpty()) summary = null;

        System.out.print("Assigned to (username): ");
        String assignedTo = scanner.nextLine().trim();
        if (assignedTo.isEmpty()) assignedTo = null;

        System.out.print("Deadline (yyyy-MM-dd or blank): ");
        String deadlineStr = scanner.nextLine().trim();
        Date deadline = null;
        if (!deadlineStr.isEmpty()) {
            deadline = new SimpleDateFormat("yyyy-MM-dd").parse(deadlineStr);
        }

        System.out.print("Estimate (e.g. 2h, 1d or blank): ");
        String estimate = scanner.nextLine().trim();
        if (estimate.isEmpty()) estimate = null;

        TaskDTO task = taskService.create(new CreateTaskRequest(title, this.projectName, assignedTo, summary, deadline, estimate));
        System.out.println("Task " + task.id() + " created successfully.");
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
