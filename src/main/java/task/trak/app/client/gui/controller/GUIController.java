package task.trak.app.client.gui.controller;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.*;
import task.trak.model.Session;
import task.trak.app.App;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.CMD_Factory;
import task.trak.api.service.TaskService;
import task.trak.api.service.ProjectService;
import task.trak.api.service.SprintService;
import task.trak.api.service.UserService;
import task.trak.api.service.BacklogService;
import task.trak.api.service.AuthService;
import task.trak.app.client.gui.viewmodel.UserViewModel;
import task.trak.app.client.gui.viewmodel.event.CommandEvent;
import task.trak.app.client.gui.viewmodel.event.CommandEventBus;
import task.trak.app.client.gui.viewmodel.event.CommandEventType;
import task.trak.app.client.gui.viewmodel.event.CommandListener;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GUIController implements App, CommandListener {

    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();
    private final Map<CommandEventType, Consumer<CommandEvent>> eventHandlers;
    private final AuthController authController;
    private final TaskController taskController;
    private final ProjectController projectController;
    private final SprintController sprintController;
    private final UserViewModel userViewModel;

    private final TaskService taskService;
    private final ProjectService projectService;
    private final SprintService sprintService;
    private final UserService userService;
    private final BacklogService backlogService;
    private final AuthService authService;

    private Consumer<Session> sessionSaver;
    private Supplier<Session> sessionLoader;

    public GUIController(TaskService taskService,
                         ProjectService projectService,
                         SprintService sprintService,
                         UserService userService,
                         BacklogService backlogService,
                         AuthService authService,
                         AuthController authController,
                         TaskController taskController,
                         ProjectController projectController,
                         SprintController sprintController,
                         UserViewModel userViewModel) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.sprintService = sprintService;
        this.userService = userService;
        this.backlogService = backlogService;
        this.authService = authService;
        this.authController = authController;
        this.taskController = taskController;
        this.projectController = projectController;
        this.sprintController = sprintController;
        this.userViewModel = userViewModel;

        this.eventHandlers = buildEventHandlers();

        TTApp.setInstance(this);
        CommandEventBus.addListener(this);
    }

    private Map<CommandEventType, Consumer<CommandEvent>> buildEventHandlers() {
        Map<CommandEventType, Consumer<CommandEvent>> map = new EnumMap<>(CommandEventType.class);

        Consumer<CommandEvent> refreshTasks = e -> taskController.refreshTasks();
        for (CommandEventType t : List.of(
                CommandEventType.TASK_CREATED, CommandEventType.TASK_UPDATED,
                CommandEventType.TASK_DELETED, CommandEventType.TASK_LIST,
                CommandEventType.COMPLETE_TASK, CommandEventType.START_TASK,
                CommandEventType.END_TASK, CommandEventType.CURRENT_TASK)) {
            map.put(t, refreshTasks);
        }

        Consumer<CommandEvent> refreshProjects = e -> projectController.refreshProjects();
        for (CommandEventType t : List.of(
                CommandEventType.PROJECT_CREATED, CommandEventType.PROJECT_UPDATED,
                CommandEventType.PROJECT_DELETED, CommandEventType.PROJECT_LIST,
                CommandEventType.ADD_MEMBER)) {
            map.put(t, refreshProjects);
        }

        Consumer<CommandEvent> refreshSprints = e -> sprintController.refreshSprints();
        for (CommandEventType t : List.of(
                CommandEventType.SPRINT_CREATED, CommandEventType.SPRINT_UPDATED,
                CommandEventType.SPRINT_DELETED, CommandEventType.SPRINT_LIST,
                CommandEventType.SPRINT_PLAN, CommandEventType.ADD_TASK)) {
            map.put(t, refreshSprints);
        }

        map.put(CommandEventType.LOGIN, e -> userViewModel.setSession(getSession()));
        map.put(CommandEventType.LOGOUT, e -> userViewModel.setSession(null));
        map.put(CommandEventType.ERROR, e -> userViewModel.setError(e.errorMessage()));
        map.put(CommandEventType.INFO, e -> userViewModel.setOutput(e.textOutput()));
        map.put(CommandEventType.DETAIL, e -> userViewModel.setOutput(e.textOutput()));

        return Collections.unmodifiableMap(map);
    }

    public void executeCommand(String input) {
        commandExecutor.submit(() -> {
            try {
                String[] args = input.trim().split("\\s+");
                CMD_Factory cmdFactory = new CMD_Factory(args);
                cmdFactory.accept(args);
                save();
            } catch (Exception e) {
                CommandEventBus.fire(new CommandEvent(
                        CommandEventType.ERROR, input, "", false, e.getMessage()));
            }
        });
    }

    @Override
    public void onCommandExecuted(CommandEvent event) {
        try {
            Consumer<CommandEvent> handler = eventHandlers.get(event.type());
            if (handler != null) {
                handler.accept(event);
            }
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    @Override
    public void save() {
        Session session = userViewModel.getSession();
        if (session != null && this.sessionSaver != null) {
            this.sessionSaver.accept(session);
        }
    }

    @Override
    public Session getSession() {
        Session session = userViewModel.getSession();
        if (session == null && this.sessionLoader != null) {
            session = this.sessionLoader.get();
            userViewModel.setSession(session);
        }
        return session;
    }

    @Override
    public void setSession(Session session) {
        userViewModel.setSession(session);
    }

    /**
     * Set the persistence callbacks for session management.
     * Must be called by the entry point before operations that need session loading/saving.
     */
    public void setSessionPersistence(Supplier<Session> loader, Consumer<Session> saver) {
        this.sessionLoader = loader;
        this.sessionSaver = saver;
    }

    /**
     * Initialize session and ensure guest account exists.
     * Call after setSessionPersistence for local mode.
     */
    public void initStore(boolean local) {
        if (!local) {
            // In REMOTE mode, no local store setup needed
            // Ensure guest exists on the server
            try {
                this.userService.create(new CreateUserRequest("guest", "Guest", "Admin", "guest@trak", "Guest1!"));
            } catch (Exception ignored) {
            }
            return;
        }

        // Clear stale session — embedded server starts fresh with no tokens
        userViewModel.setSession(null);
        if (this.sessionSaver != null) {
            this.sessionSaver.accept(null);
        }

        // Ensure guest admin account exists (create is unprotected; ignore if already exists)
        try {
            this.userService.create(new CreateUserRequest("guest", "Guest", "Admin", "guest@trak", "Guest1!"));
        } catch (Exception ignored) {
        }
    }

    public void clearViewModels() {
        taskController.getViewModel().setAll(List.of());
        projectController.getViewModel().setAll(List.of());
        sprintController.getViewModel().setAll(List.of());
        taskController.getViewModel().clearCache();
        projectController.getViewModel().clearCache();
        sprintController.getViewModel().clearCache();
    }

    /**
     * Refresh all data off-EDT, then fire notifications in one batch.
     * Call from a background thread.
     */
    public void refreshAll() {
        try {
            Session session = userViewModel.getSession();
            String user = session != null ? session.getLogged_in_user() : null;

            var tasks = user != null
                    ? taskService.listByAssignee(user)
                    : java.util.List.<task.trak.model.dto.TaskDTO>of();
            var projects = user != null
                    ? projectService.listByUser(user)
                    : projectService.listAll();
            var sprints = sprintService.listAll();

            taskController.getViewModel().setAllSilent(tasks);
            projectController.getViewModel().setAllSilent(projects);
            sprintController.getViewModel().setAllSilent(sprints);

            javax.swing.SwingUtilities.invokeLater(() -> {
                projectController.getViewModel().notifyObservers(
                        task.trak.app.client.gui.viewmodel.ViewModelChangeType.PROJECTS);
                taskController.getViewModel().notifyObservers(
                        task.trak.app.client.gui.viewmodel.ViewModelChangeType.TASKS);
                sprintController.getViewModel().notifyObservers(
                        task.trak.app.client.gui.viewmodel.ViewModelChangeType.SPRINTS);
            });
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public AuthController getAuthController() {
        return authController;
    }

    public TaskController getTaskController() {
        return taskController;
    }

    public ProjectController getProjectController() {
        return projectController;
    }

    public SprintController getSprintController() {
        return sprintController;
    }

    public void seedData() {
        try {
            // guest account already created by initStore — login to get bearer token
            Session session = this.authService.login("guest", "Guest1!");
            userViewModel.setSession(session);
            save();

            // Skip if data already exists
            if (this.taskService.listAll().size() > 0) return;

            System.out.println("Seeding test data...");

            // 1 project
            ProjectDTO project = this.projectService.create(
                    new CreateProjectRequest("FocusApp", "Solo dev focus app", "guest", List.of()));

            // 20 tasks — exact titles/estimates from test-gui.sh
            String[][] tasks = {
                    {"Fix typo in README",    "Quick fix",              "1m"},
                    {"Update .gitignore",     "Add build artifacts",    "1m"},
                    {"Rename variable",       "Refactor naming",        "2m"},
                    {"Add license file",      "MIT license",            "1m"},
                    {"Write login endpoint",  "POST /auth/login",       "3m"},
                    {"Add input validation",  "Validate email format",  "2m"},
                    {"Create user model",     "User schema",            "3m"},
                    {"Setup database",        "Init DuckDB tables",     "2m"},
                    {"Write unit test",       "Test auth flow",         "3m"},
                    {"Fix null pointer",      "Handle null session",    "1m"},
                    {"Add error dialog",      "Show user-facing errors","2m"},
                    {"Style buttons",         "Uniform button sizes",   "1m"},
                    {"Add delete confirm",    "Confirm before delete",  "2m"},
                    {"Refactor controller",   "Extract methods",        "3m"},
                    {"Add loading spinner",   "Show during fetch",      "2m"},
                    {"Fix date formatting",   "Use ISO-8601",           "1m"},
                    {"Write API docs",        "Document endpoints",     "3m"},
                    {"Add sort dropdown",     "Sort by date/estimate",  "2m"},
                    {"Fix card hover",        "Gold glow on hover",     "1m"},
                    {"Deploy to prod",        "Final deploy",           "3m"},
            };

            List<Long> taskIds = new ArrayList<>();
            for (String[] t : tasks) {
                TaskDTO created = this.taskService.create(
                        new CreateTaskRequest(t[0], "FocusApp", "guest", t[1], null, t[2]));
                taskIds.add(created.id());
            }

            // Sprint with first 10 tasks
            this.sprintService.create(new CreateSprintRequest("Sprint1", "FocusApp"));
            this.sprintService.update(new UpdateSprintRequest(
                    "Sprint1", "FocusApp", "2026-05-14", "2026-05-21",
                    taskIds.subList(0, 10), null, null));

            // 3 tasks in progress
            this.taskService.updateById(new UpdateTaskRequest(taskIds.get(0), null, "INPROGRESS", null, null, null, null));
            this.taskService.updateById(new UpdateTaskRequest(taskIds.get(4), null, "INPROGRESS", null, null, null, null));
            this.taskService.updateById(new UpdateTaskRequest(taskIds.get(8), null, "INPROGRESS", null, null, null, null));

            // 2 tasks complete
            this.taskService.updateById(new UpdateTaskRequest(taskIds.get(1), null, "COMPLETE", null, null, null, null));
            this.taskService.updateById(new UpdateTaskRequest(taskIds.get(3), null, "COMPLETE", null, null, null, null));

            System.out.println("Seeded: 1 project, 20 tasks, 1 sprint (login: guest / guest)");
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }
}
