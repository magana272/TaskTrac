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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceListTest {

    private static final String TEST_STORE = ".store_list_test";
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
    public void TestTaskListAll() throws InterruptedException {
        TaskService taskService = new TrakTaskService();
        taskService.create("Task 1", "Proj", "alice", null, null, null);
        Thread.sleep(5);
        taskService.create("Task 2", "Proj", "bob", null, null, null);
        Thread.sleep(5);
        taskService.create("Task 3", "Proj", "alice", null, null, null);

        List<TaskDTO> all = taskService.listAll();
        assertEquals(3, all.size());
    }

    @Test
    public void TestTaskListByAssignee() throws InterruptedException {
        TaskService taskService = new TrakTaskService();
        taskService.create("Task A", "Proj", "alice", null, null, null);
        Thread.sleep(5);
        taskService.create("Task B", "Proj", "bob", null, null, null);
        Thread.sleep(5);
        taskService.create("Task C", "Proj", "alice", null, null, null);

        List<TaskDTO> aliceTasks = taskService.listByAssignee("alice");
        assertEquals(2, aliceTasks.size());
        assertTrue(aliceTasks.stream().allMatch(t -> "alice".equals(t.assignedTo())));

        List<TaskDTO> bobTasks = taskService.listByAssignee("bob");
        assertEquals(1, bobTasks.size());
    }

    @Test
    public void TestTaskListByAssigneeEmpty() {
        TaskService taskService = new TrakTaskService();
        List<TaskDTO> tasks = taskService.listByAssignee("nobody");
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void TestProjectListAll() {
        UserService userService = new TrakUserService();
        userService.create("owner1", "Own", "Er", null, null);

        ProjectService projectService = new TrakProjectService();
        projectService.create("Proj1", null, "owner1", null);
        projectService.create("Proj2", null, "owner1", null);

        List<ProjectDTO> all = projectService.listAll();
        assertEquals(2, all.size());
    }

    @Test
    public void TestProjectListByOwner() {
        UserService userService = new TrakUserService();
        userService.create("ownerA", "Own", "A", null, null);
        userService.create("ownerB", "Own", "B", null, null);

        ProjectService projectService = new TrakProjectService();
        projectService.create("ProjA", null, "ownerA", null);
        projectService.create("ProjB", null, "ownerB", null);

        List<ProjectDTO> aProjects = projectService.listByUser("ownerA");
        assertEquals(1, aProjects.size());
        assertEquals("ProjA", aProjects.get(0).projectName());
    }

    @Test
    public void TestProjectListByMember() {
        UserService userService = new TrakUserService();
        userService.create("projowner", "Own", "Er", null, null);
        userService.create("member1", "Mem", "Ber", null, null);

        ProjectService projectService = new TrakProjectService();
        projectService.create("TeamProj", null, "projowner", List.of("member1"));

        List<ProjectDTO> memberProjects = projectService.listByUser("member1");
        assertEquals(1, memberProjects.size());
        assertEquals("TeamProj", memberProjects.get(0).projectName());
    }

    @Test
    public void TestProjectListByUserEmpty() {
        ProjectService projectService = new TrakProjectService();
        List<ProjectDTO> projects = projectService.listByUser("nobody");
        assertTrue(projects.isEmpty());
    }
}
