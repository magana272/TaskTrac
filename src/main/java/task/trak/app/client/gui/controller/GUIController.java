package task.trak.app.client.gui.controller;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.*;
import task.trak.model.Session;
import task.trak.app.App;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.CMD_Factory;
import task.trak.app.client.http.TaskHttpService;
import task.trak.app.client.http.ProjectHttpService;
import task.trak.app.client.http.SprintHttpService;
import task.trak.app.client.http.UserHttpService;
import task.trak.app.client.http.BacklogHttpService;
import task.trak.app.client.http.AuthHttpService;
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
import java.util.stream.Collectors;

public class GUIController implements App, CommandListener {

    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();
    private final AuthController authController;
    private final TaskController taskController;
    private final ProjectController projectController;
    private final SprintController sprintController;
    private final UserViewModel userViewModel;

    private final TaskHttpService taskService;
    private final ProjectHttpService projectService;
    private final SprintHttpService sprintService;
    private final UserHttpService userService;
    private final BacklogHttpService backlogService;
    private final AuthHttpService authService;

    private Consumer<Session> sessionSaver;
    private Supplier<Session> sessionLoader;

    public GUIController(TaskHttpService taskService,
                         ProjectHttpService projectService,
                         SprintHttpService sprintService,
                         UserHttpService userService,
                         BacklogHttpService backlogService,
                         AuthHttpService authService,
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

        TTApp.setInstance(this);
        CommandEventBus.addListener(this);
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
            CommandEventType type = event.type();

            switch (type) {
                case TASK_CREATED, TASK_UPDATED, TASK_DELETED, TASK_LIST,
                     COMPLETE_TASK, START_TASK, END_TASK, CURRENT_TASK:
                    taskController.refreshTasks();
                    break;
                case PROJECT_CREATED, PROJECT_UPDATED, PROJECT_DELETED, PROJECT_LIST,
                     ADD_MEMBER:
                    projectController.refreshProjects();
                    break;
                case SPRINT_CREATED, SPRINT_UPDATED, SPRINT_DELETED, SPRINT_LIST,
                     SPRINT_PLAN, ADD_TASK:
                    sprintController.refreshSprints();
                    break;
                case LOGIN:
                    userViewModel.setSession(getSession());
                    break;
                case LOGOUT:
                    userViewModel.setSession(null);
                    break;
                case ERROR:
                    userViewModel.setError(event.errorMessage());
                    break;
                case INFO, DETAIL:
                    userViewModel.setOutput(event.textOutput());
                    break;
                default:
                    break;
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
                this.userService.create(new CreateUserRequest("guest", "Guest", "Admin", "guest@trak", "guest"));
            } catch (Exception ignored) {
            }
            return;
        }

        if (this.sessionLoader != null) {
            Session session = this.sessionLoader.get();
            userViewModel.setSession(session);
        }

        // Ensure guest admin account exists (create is unprotected; ignore if already exists)
        try {
            this.userService.create(new CreateUserRequest("guest", "Guest", "Admin", "guest@trak", "guest"));
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

    private void seedData() {
        try {
            // Skip if data already exists
            if (this.taskService.listAll().size() > 0) return;

            Random rand = new Random(42);

            String[] firstNames = {"Alice", "Bob", "Carlos", "Diana", "Eve", "Frank", "Grace", "Hank",
                    "Ivy", "Jack", "Karen", "Leo", "Mia", "Noah", "Olivia", "Paul", "Quinn", "Rosa", "Sam", "Tina"};
            String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
                    "Thomas", "Taylor", "Moore", "Jackson", "Martin"};
            String[] projectNames = {"MobileApp", "WebPortal", "DataPipeline", "AuthService", "Analytics",
                    "ChatBot", "PaymentGateway", "CMS", "Monitoring", "DevTools"};
            String[] prefixes = {"Implement", "Fix", "Refactor", "Test", "Design", "Review", "Update",
                    "Deploy", "Document", "Optimize", "Debug", "Configure", "Migrate", "Build"};
            String[] subjects = {"login flow", "dashboard", "API endpoint", "database schema", "auth module",
                    "unit tests", "CI pipeline", "error handling", "caching layer", "UI component",
                    "search feature", "notification service", "user profile", "settings page",
                    "export function", "import tool", "logging", "rate limiter", "webhook handler", "background job"};
            String[] statuses = {"READY", "INPROGRESS", "COMPLETE"};
            String[] estimates = {"1h", "2h", "4h", "8h", "1d", "2d", "3d", "5d"};

            System.out.println("Seeding test data...");

            // 20 users (guest already exists)
            List<String> usernames = new ArrayList<>();
            usernames.add("guest");
            for (int i = 0; i < 19; i++) {
                String username = firstNames[i].toLowerCase() + (i + 1);
                if (this.userService.getByUsername(username) == null) {
                    this.userService.create(new CreateUserRequest(username, firstNames[i], lastNames[i],
                            firstNames[i].toLowerCase() + "@company.com", "password"));
                }
                usernames.add(username);
            }

            // 10 projects -- guest owns 0-3, member of 4-6
            for (int i = 0; i < 10; i++) {
                if (this.projectService.getByName(projectNames[i]) != null) continue;

                String owner;
                List<String> members = new ArrayList<>();

                if (i < 4) {
                    owner = "guest";
                    Set<String> picked = new HashSet<>();
                    while (picked.size() < 2 + rand.nextInt(3)) {
                        picked.add(usernames.get(1 + rand.nextInt(19)));
                    }
                    members.addAll(picked);
                } else {
                    owner = usernames.get(1 + (i - 4) % 19);
                    Set<String> picked = new HashSet<>();
                    if (i <= 6) picked.add("guest");
                    while (picked.size() < 2 + rand.nextInt(4)) {
                        String m = usernames.get(rand.nextInt(20));
                        if (!m.equals(owner)) picked.add(m);
                    }
                    members.addAll(picked);
                }

                this.projectService.create(new CreateProjectRequest(projectNames[i], "Project for " + projectNames[i] + " development", owner, members));
            }

            // 1000 tasks
            for (int i = 0; i < 1000; i++) {
                String project = projectNames[i % 10];
                String assignee = usernames.get(i % 20);
                String title = prefixes[rand.nextInt(prefixes.length)] + " " + subjects[rand.nextInt(subjects.length)] + " #" + (i + 1);
                String summary = "Task " + (i + 1) + " for " + project;
                String estimate = estimates[rand.nextInt(estimates.length)];

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1 + rand.nextInt(30));

                TaskDTO task = this.taskService.create(new CreateTaskRequest(title, project, assignee, summary, cal.getTime(), estimate));

                String status = statuses[rand.nextInt(statuses.length)];
                if (!"READY".equals(status)) {
                    this.taskService.updateById(new UpdateTaskRequest(task.id(), null, status, null, null, null));
                }
            }

            // 2 sprints per project (20 total), each with ~50 tasks
            List<TaskDTO> allTasks = this.taskService.listAll();
            String[] sprintNames = {"Sprint 1", "Sprint 2"};

            for (int i = 0; i < 10; i++) {
                String project = projectNames[i];
                // Get tasks for this project
                List<TaskDTO> projectTasks = allTasks.stream()
                        .filter(t -> project.equals(t.projectName()))
                        .collect(Collectors.toList());

                for (int s = 0; s < 2; s++) {
                    String sprintName = sprintNames[s];
                    this.sprintService.create(new CreateSprintRequest(sprintName, project));

                    // Set dates: sprint 1 starts now, sprint 2 starts in 2 weeks
                    Calendar start = Calendar.getInstance();
                    start.add(Calendar.DAY_OF_MONTH, s * 14);
                    Calendar end = (Calendar) start.clone();
                    end.add(Calendar.DAY_OF_MONTH, 13);

                    String startStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
                    String endStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(end.getTime());

                    // Assign half the project's tasks to each sprint
                    int from = s * (projectTasks.size() / 2);
                    int to = (s == 0) ? projectTasks.size() / 2 : projectTasks.size();
                    List<Long> taskIds = new ArrayList<>();
                    for (int t = from; t < to; t++) {
                        taskIds.add(projectTasks.get(t).id());
                    }
                    this.sprintService.update(new UpdateSprintRequest(sprintName, project, startStr, endStr, taskIds));
                }
            }

            System.out.println("Seeded: 20 users, 10 projects, 1000 tasks, 20 sprints.");

            // Auto-login as guest
            userViewModel.setSession(new Session("guest"));
            save();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }
}
