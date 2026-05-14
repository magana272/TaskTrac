package task.trak.store;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.TaskService;
import task.trak.api.service.UserService;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.service.project.TrakProjectService;
import task.trak.app.server.service.task.TrakTaskService;
import task.trak.app.server.service.user.TrakUserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Seeds the store with 20 users, 10 projects, 1000 tasks.
 * Guest is owner of some projects and member of others.
 */
public class SeedDataTest {

    private static final String TEST_STORE = ".store_seed_test";
    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Carlos", "Diana", "Eve", "Frank", "Grace", "Hank",
            "Ivy", "Jack", "Karen", "Leo", "Mia", "Noah", "Olivia", "Paul",
            "Quinn", "Rosa", "Sam", "Tina"
    };
    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
            "Thomas", "Taylor", "Moore", "Jackson", "Martin"
    };
    private static final String[] PROJECT_NAMES = {
            "MobileApp", "WebPortal", "DataPipeline", "AuthService", "Analytics",
            "ChatBot", "PaymentGateway", "CMS", "Monitoring", "DevTools"
    };
    private static final String[] TASK_PREFIXES = {
            "Implement", "Fix", "Refactor", "Test", "Design", "Review", "Update",
            "Deploy", "Document", "Optimize", "Debug", "Configure", "Migrate", "Build"
    };
    private static final String[] TASK_SUBJECTS = {
            "login flow", "dashboard", "API endpoint", "database schema", "auth module",
            "unit tests", "CI pipeline", "error handling", "caching layer", "UI component",
            "search feature", "notification service", "user profile", "settings page",
            "export function", "import tool", "logging", "rate limiter", "webhook handler",
            "background job"
    };
    private static final String[] STATUSES = {"READY", "INPROGRESS", "COMPLETE"};
    private static final String[] ESTIMATES = {"1h", "2h", "4h", "8h", "1d", "2d", "3d", "5d"};
    private String originalStoreDir;
    private DAOFactory.Format originalFormat;

    @Before
    public void setUp() {
        originalStoreDir = TTApp.storedir;
        originalFormat = DAOFactory.getFormat();
        TTApp.storedir = TEST_STORE;
        DAOFactory.setFormat(DAOFactory.Format.JSON);
        new File(TEST_STORE).mkdirs();
    }

    @After
    public void tearDown() {
        File dir = new File(TEST_STORE);
        if (dir.exists()) {
            for (File f : dir.listFiles()) f.delete();
            dir.delete();
        }
        TTApp.storedir = originalStoreDir;
        DAOFactory.setFormat(originalFormat);
    }

    @Test
    public void TestSeedData() throws InterruptedException {
        UserService userService = new TrakUserService();
        ProjectService projectService = new TrakProjectService();
        TaskService taskService = new TrakTaskService();
        Random rand = new Random(42);

        // --- Create 20 users (including guest) ---
        List<String> usernames = new ArrayList<>();
        userService.create("guest", "Guest", "Admin", "guest@trak", "guest");
        usernames.add("guest");

        for (int i = 0; i < 19; i++) {
            String username = FIRST_NAMES[i].toLowerCase() + (i + 1);
            userService.create(username, FIRST_NAMES[i], LAST_NAMES[i],
                    FIRST_NAMES[i].toLowerCase() + "@company.com", "password");
            usernames.add(username);
            Thread.sleep(2); // ensure unique IDs
        }
        assertEquals(20, usernames.size());

        // --- Create 10 projects ---
        // Guest owns projects 0-3, other users own 4-9
        // Guest is member of projects 4-6
        List<String> projectNames = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String owner;
            List<String> members = new ArrayList<>();

            if (i < 4) {
                // Guest-owned projects
                owner = "guest";
                // Add 2-4 random members
                int memberCount = 2 + rand.nextInt(3);
                Set<String> picked = new HashSet<>();
                while (picked.size() < memberCount) {
                    String m = usernames.get(1 + rand.nextInt(19)); // skip guest
                    picked.add(m);
                }
                members.addAll(picked);
            } else {
                // Owned by other users
                owner = usernames.get(1 + (i - 4) % 19);
                // Add some members
                int memberCount = 2 + rand.nextInt(4);
                Set<String> picked = new HashSet<>();
                if (i <= 6) picked.add("guest"); // guest is member of projects 4-6
                while (picked.size() < memberCount) {
                    String m = usernames.get(rand.nextInt(20));
                    if (!m.equals(owner)) picked.add(m);
                }
                members.addAll(picked);
            }

            String summary = "Project for " + PROJECT_NAMES[i] + " development";
            projectService.create(PROJECT_NAMES[i], summary, owner, members);
            projectNames.add(PROJECT_NAMES[i]);
            Thread.sleep(2);
        }
        assertEquals(10, projectNames.size());

        // --- Create 1000 tasks spread across projects ---
        for (int i = 0; i < 1000; i++) {
            String project = projectNames.get(i % 10);
            String assignee = usernames.get(i % 20);
            String prefix = TASK_PREFIXES[rand.nextInt(TASK_PREFIXES.length)];
            String subject = TASK_SUBJECTS[rand.nextInt(TASK_SUBJECTS.length)];
            String title = prefix + " " + subject + " #" + (i + 1);
            String summary = "Task " + (i + 1) + " for " + project;
            String estimate = ESTIMATES[rand.nextInt(ESTIMATES.length)];

            // Deadline 1-30 days from now
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1 + rand.nextInt(30));
            Date deadline = cal.getTime();

            TaskDTO task = taskService.create(title, project, assignee, summary, deadline, estimate);
            assertNotNull(task);

            // Set some tasks to INPROGRESS or COMPLETE
            String status = STATUSES[rand.nextInt(STATUSES.length)];
            if (!"READY".equals(status)) {
                taskService.updateById(task.id(), null, status, null, null);
            }

            Thread.sleep(1); // ensure unique IDs
        }

        // --- Verify counts ---
        List<TaskDTO> allTasks = taskService.listAll();
        assertEquals(1000, allTasks.size());

        List<ProjectDTO> allProjects = projectService.listAll();
        assertEquals(10, allProjects.size());

        // Verify guest owns some projects
        List<ProjectDTO> guestProjects = projectService.listByUser("guest");
        assertTrue("Guest should own or be member of projects", guestProjects.size() >= 4);

        // Verify task distribution — each project should have ~100 tasks
        for (ProjectDTO p : allProjects) {
            assertTrue("Project " + p.projectName() + " should have tasks", p.taskCount() > 0);
        }

        // Verify guest has tasks assigned
        List<TaskDTO> guestTasks = taskService.listByAssignee("guest");
        assertTrue("Guest should have assigned tasks", guestTasks.size() > 0);

        // Verify status distribution
        long ready = allTasks.stream().filter(t -> "READY".equals(t.status())).count();
        long inProgress = allTasks.stream().filter(t -> "INPROGRESS".equals(t.status())).count();
        long complete = allTasks.stream().filter(t -> "COMPLETE".equals(t.status())).count();
        assertTrue("Should have READY tasks", ready > 0);
        assertTrue("Should have INPROGRESS tasks", inProgress > 0);
        assertTrue("Should have COMPLETE tasks", complete > 0);
        assertEquals(1000, ready + inProgress + complete);
    }
}
