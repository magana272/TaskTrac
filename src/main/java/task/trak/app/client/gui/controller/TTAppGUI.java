package task.trak.app.client.gui.controller;

import task.trak.api.dto.SprintDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.model.Session;
import task.trak.api.service.*;
import task.trak.app.App;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.CMD_Factory;
import task.trak.app.client.gui.model.CommandEvent;
import task.trak.app.client.gui.model.CommandEventBus;
import task.trak.app.client.gui.model.CommandEventType;
import task.trak.app.client.gui.model.CommandListener;
import task.trak.app.client.gui.view.MainFrame;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TTAppGUI implements App, CommandListener {
    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();
    private Session session;
    private MainFrame mainFrame;
    private Consumer<Session> sessionSaver;
    private Supplier<Session> sessionLoader;

    public TTAppGUI(boolean seedTestData, boolean local) {
        TTApp.setInstance(this);

        if (seedTestData) {
            seedData();
        }

        CommandEventBus.addListener(this);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            this.mainFrame = new MainFrame(this);
            this.mainFrame.setVisible(true);
        });
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
                UserService userService = ServiceFactory.userService();
                if (userService.getByUsername("guest") == null) {
                    userService.create("guest", "Guest", "Admin", "guest@trak", "guest");
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not connect to server to check guest account.");
            }
            return;
        }

        if (this.sessionLoader != null) {
            this.session = this.sessionLoader.get();
        }

        // Ensure guest admin account exists
        UserService userService = ServiceFactory.userService();
        if (userService.getByUsername("guest") == null) {
            userService.create("guest", "Guest", "Admin", "guest@trak", "guest");
        }
    }

    private void seedData() {
        UserService userService = ServiceFactory.userService();
        ProjectService projectService = ServiceFactory.projectService();
        TaskService taskService = ServiceFactory.taskService();

        // Skip if data already exists
        if (taskService.listAll().size() > 0) return;

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
            if (userService.getByUsername(username) == null) {
                userService.create(username, firstNames[i], lastNames[i],
                        firstNames[i].toLowerCase() + "@company.com", "password");
            }
            usernames.add(username);
        }

        // 10 projects -- guest owns 0-3, member of 4-6
        for (int i = 0; i < 10; i++) {
            if (projectService.getByName(projectNames[i]) != null) continue;

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

            projectService.create(projectNames[i], "Project for " + projectNames[i] + " development", owner, members);
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

            TaskDTO task = taskService.create(title, project, assignee, summary, cal.getTime(), estimate);

            String status = statuses[rand.nextInt(statuses.length)];
            if (!"READY".equals(status)) {
                taskService.updateById(task.id(), null, status, null, null);
            }
        }

        // 2 sprints per project (20 total), each with ~50 tasks
        SprintService sprintService = ServiceFactory.sprintService();
        List<TaskDTO> allTasks = taskService.listAll();
        String[] sprintNames = {"Sprint 1", "Sprint 2"};

        for (int i = 0; i < 10; i++) {
            String project = projectNames[i];
            // Get tasks for this project
            List<TaskDTO> projectTasks = allTasks.stream()
                    .filter(t -> project.equals(t.projectName()))
                    .collect(java.util.stream.Collectors.toList());

            for (int s = 0; s < 2; s++) {
                String sprintName = sprintNames[s];
                SprintDTO sprint = sprintService.create(sprintName, project);

                // Set dates: sprint 1 starts now, sprint 2 starts in 2 weeks
                Calendar start = Calendar.getInstance();
                start.add(Calendar.DAY_OF_MONTH, s * 14);
                Calendar end = (Calendar) start.clone();
                end.add(Calendar.DAY_OF_MONTH, 13);

                String startStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
                String endStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(end.getTime());
                sprintService.updateByNameAndProject(sprintName, project, startStr, endStr);

                // Assign half the project's tasks to each sprint
                int from = s * (projectTasks.size() / 2);
                int to = (s == 0) ? projectTasks.size() / 2 : projectTasks.size();
                List<Long> taskIds = new ArrayList<>();
                for (int t = from; t < to; t++) {
                    taskIds.add(projectTasks.get(t).id());
                }
                sprintService.updateTaskIds(String.valueOf(sprint.id()), taskIds);
            }
        }

        System.out.println("Seeded: 20 users, 10 projects, 1000 tasks, 20 sprints.");

        // Auto-login as guest
        this.session = new Session("guest");
        save();
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
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
                mainFrame.displayCommandResult(event);
            }
        });
    }

    @Override
    public void save() {
        if (this.session != null && this.sessionSaver != null) {
            this.sessionSaver.accept(this.session);
        }
    }

    @Override
    public Session getSession() {
        if (this.session == null && this.sessionLoader != null) {
            this.session = this.sessionLoader.get();
        }
        return this.session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }
}
