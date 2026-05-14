package task.trak.api.service;

import task.trak.app.client.http.*;
import task.trak.app.server.service.auth.TrakAuthService;
import task.trak.app.server.service.backlog.TrakBacklogService;
import task.trak.app.server.service.project.TrakProjectService;
import task.trak.app.server.service.sprint.TrakSprintService;
import task.trak.app.server.service.task.TrakTaskService;
import task.trak.app.server.service.user.TrakUserService;

import java.util.function.Supplier;

/**
 * Service locator for all service interfaces.
 * Entry points (ServerMain, CLIMain, GUIMain, Main) register the concrete suppliers
 * before any commands run.
 */
public class ServiceFactory {

    private static Supplier<TaskService> taskServiceSupplier;
    private static Supplier<UserService> userServiceSupplier;
    private static Supplier<ProjectService> projectServiceSupplier;
    private static Supplier<SprintService> sprintServiceSupplier;
    private static Supplier<BacklogService> backlogServiceSupplier;
    private static Supplier<AuthService> authServiceSupplier;

    public static void register(
            Supplier<TaskService> task,
            Supplier<UserService> user,
            Supplier<ProjectService> project,
            Supplier<SprintService> sprint,
            Supplier<BacklogService> backlog,
            Supplier<AuthService> auth) {
        taskServiceSupplier = task;
        userServiceSupplier = user;
        projectServiceSupplier = project;
        sprintServiceSupplier = sprint;
        backlogServiceSupplier = backlog;
        authServiceSupplier = auth;
    }

    public static TaskService taskService() {
        if (taskServiceSupplier == null) throw new IllegalStateException("ServiceFactory not initialized");
        return taskServiceSupplier.get();
    }

    public static UserService userService() {
        if (userServiceSupplier == null) throw new IllegalStateException("ServiceFactory not initialized");
        return userServiceSupplier.get();
    }

    public static ProjectService projectService() {
        if (projectServiceSupplier == null) throw new IllegalStateException("ServiceFactory not initialized");
        return projectServiceSupplier.get();
    }

    public static SprintService sprintService() {
        if (sprintServiceSupplier == null) throw new IllegalStateException("ServiceFactory not initialized");
        return sprintServiceSupplier.get();
    }

    public static BacklogService backlogService() {
        if (backlogServiceSupplier == null) throw new IllegalStateException("ServiceFactory not initialized");
        return backlogServiceSupplier.get();
    }

    public static AuthService authService() {
        if (authServiceSupplier == null) throw new IllegalStateException("ServiceFactory not initialized");
        return authServiceSupplier.get();
    }

    /**
     * Register HTTP client implementations (for CLI/GUI client mode)
     */
    public static void registerHttpServices() {
        register(
                TaskHttpService::new,
                UserHttpService::new,
                ProjectHttpService::new,
                SprintHttpService::new,
                BacklogHttpService::new,
                AuthHttpService::new
        );
    }

    /**
     * Register direct local service implementations (for server or local mode)
     */
    public static void registerLocalServices() {
        register(
                TrakTaskService::new,
                TrakUserService::new,
                TrakProjectService::new,
                TrakSprintService::new,
                TrakBacklogService::new,
                TrakAuthService::new
        );
    }
}
