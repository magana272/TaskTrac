package task.trak.gui;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.SprintDTO;
import task.trak.model.Session;
import task.trak.app.client.gui.viewmodel.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the Observer pattern on ViewModels.
 * Verifies addObserver(), removeObserver(), notifyObservers() behavior
 * including cross-domain observation.
 */
public class ObserverPatternTest {

    private static final String TEST_CACHE = "src/test/.cache";
    private String originalCacheDir;

    @Before
    public void setUp() {
        originalCacheDir = ObservableViewModel.CACHE_DIR;
        ObservableViewModel.CACHE_DIR = TEST_CACHE;
    }

    @After
    public void tearDown() {
        File dir = new File(TEST_CACHE);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) f.delete();
            dir.delete();
        }
        ObservableViewModel.CACHE_DIR = originalCacheDir;
    }

    // --- addObserver / notifyObservers ---

    @Test
    public void testObserverReceivesNotificationOnSetAll() {
        TaskViewModel vm = new TaskViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.setAll(List.of(new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null)));

        assertEquals(1, received.size());
        assertEquals(ViewModelChangeType.TASKS, received.get(0));
    }

    @Test
    public void testObserverReceivesNotificationOnCreate() {
        ProjectViewModel vm = new ProjectViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.create(new ProjectDTO(1L, "Proj", "desc", null, "owner", List.of(), 0, 0, 0));

        assertEquals(1, received.size());
        assertEquals(ViewModelChangeType.PROJECTS, received.get(0));
    }

    @Test
    public void testObserverReceivesNotificationOnUpdate() {
        SprintViewModel vm = new SprintViewModel();
        vm.setAll(List.of(new SprintDTO(1L, "Proj", "Sprint1", List.of(), null, null)));
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.update(new SprintDTO(1L, "Proj", "Sprint1-Updated", List.of(), null, null));

        assertEquals(1, received.size());
        assertEquals(ViewModelChangeType.SPRINTS, received.get(0));
    }

    @Test
    public void testObserverReceivesNotificationOnDelete() {
        TaskViewModel vm = new TaskViewModel();
        TaskDTO task = new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null);
        vm.setAll(List.of(task));
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.delete(task);

        assertEquals(1, received.size());
        assertEquals(ViewModelChangeType.TASKS, received.get(0));
    }

    // --- removeObserver ---

    @Test
    public void testRemovedObserverDoesNotReceiveNotifications() {
        TaskViewModel vm = new TaskViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        ViewModelChangeListener observer = received::add;
        vm.addObserver(observer);
        vm.removeObserver(observer);

        vm.setAll(List.of());

        assertTrue("Removed observer should not receive notifications", received.isEmpty());
    }

    // --- Multiple observers ---

    @Test
    public void testMultipleObserversAllNotified() {
        ProjectViewModel vm = new ProjectViewModel();
        List<String> log = new ArrayList<>();
        vm.addObserver(type -> log.add("observer1:" + type));
        vm.addObserver(type -> log.add("observer2:" + type));

        vm.setAll(List.of());

        assertEquals(2, log.size());
        assertTrue(log.contains("observer1:PROJECTS"));
        assertTrue(log.contains("observer2:PROJECTS"));
    }

    // --- Cross-domain observation ---

    @Test
    public void testCrossDomainObservation_TaskViewObservesProjectViewModel() {
        ProjectViewModel projectVM = new ProjectViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();

        // Simulate TasksView observing ProjectViewModel
        projectVM.addObserver(received::add);

        // Project is created
        projectVM.create(new ProjectDTO(1L, "NewProj", "desc", null, "owner", List.of(), 0, 0, 0));

        // Observer (TasksView) was notified
        assertTrue("Cross-domain observer should be notified", received.contains(ViewModelChangeType.PROJECTS));

        // Fresh project data is available
        assertEquals(1, projectVM.get().size());
        assertEquals("NewProj", projectVM.get().get(0).projectName());
    }

    @Test
    public void testCrossDomainObservation_SprintViewObservesAllThree() {
        TaskViewModel taskVM = new TaskViewModel();
        ProjectViewModel projectVM = new ProjectViewModel();
        SprintViewModel sprintVM = new SprintViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();

        // Simulate SprintView observing all 3
        ViewModelChangeListener sprintViewObserver = received::add;
        taskVM.addObserver(sprintViewObserver);
        projectVM.addObserver(sprintViewObserver);
        sprintVM.addObserver(sprintViewObserver);

        // Changes in all 3 domains
        taskVM.setAll(List.of(new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null)));
        projectVM.setAll(List.of(new ProjectDTO(1L, "P", "d", null, "o", List.of(), 0, 0, 0)));
        sprintVM.setAll(List.of(new SprintDTO(1L, "P", "S1", List.of(), null, null)));

        assertEquals(3, received.size());
        assertTrue(received.contains(ViewModelChangeType.TASKS));
        assertTrue(received.contains(ViewModelChangeType.PROJECTS));
        assertTrue(received.contains(ViewModelChangeType.SPRINTS));
    }

    // --- UserViewModel observer ---

    @Test
    public void testSessionChangeNotifiesObservers() {
        UserViewModel vm = new UserViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.setSession(new Session("testuser"));

        assertTrue(received.contains(ViewModelChangeType.SESSION));
    }

    @Test
    public void testErrorChangeNotifiesObservers() {
        UserViewModel vm = new UserViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.setError("Something failed");

        assertTrue(received.contains(ViewModelChangeType.ERROR));
    }

    // --- Data freshness after notification ---

    // --- SESSION null notification ---

    @Test
    public void testSessionNullNotifiesObservers() {
        UserViewModel vm = new UserViewModel();
        vm.setSession(new Session("testuser"));
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.setSession(null);

        assertTrue(received.contains(ViewModelChangeType.SESSION));
        assertNull(vm.getSession());
    }

    // --- OUTPUT notification ---

    @Test
    public void testOutputChangeNotifiesObservers() {
        UserViewModel vm = new UserViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.setOutput("Hello World");

        assertTrue(received.contains(ViewModelChangeType.OUTPUT));
        assertEquals("Hello World", vm.getLastOutput());
    }

    // --- Multiple setAll fires multiple notifications ---

    @Test
    public void testMultipleSetAllFiresMultipleNotifications() {
        TaskViewModel vm = new TaskViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);

        vm.setAll(List.of());
        vm.setAll(List.of());
        vm.setAll(List.of());

        assertEquals(3, received.size());
    }

    // --- Observer exception isolation ---

    @Test
    public void testObserverExceptionDoesNotBreakOtherObservers() {
        TaskViewModel vm = new TaskViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();

        // First observer throws
        vm.addObserver(type -> { throw new RuntimeException("bad observer"); });
        // Second observer records
        vm.addObserver(received::add);

        vm.setAll(List.of());

        // With exception isolation, second observer should still be notified
        assertEquals("Second observer should receive notification despite first observer throwing",
                1, received.size());
        assertEquals(ViewModelChangeType.TASKS, received.get(0));
    }

    // --- Serialization ---

    @Test
    public void testSaveDoesNotThrowOnEmptyViewModel() {
        // save() with empty data should not throw
        TaskViewModel vm = new TaskViewModel();
        vm.save(); // no exception expected
    }

    @Test
    public void testLoadOnMissingFileIsNoOp() {
        // load() when cache file doesn't exist should not throw or change state
        TaskViewModel vm = new TaskViewModel();
        vm.setAll(List.of(new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null)));

        // Delete cache file if it exists
        File cacheFile = new File(TEST_CACHE, "task_viewmodel.ser");
        if (cacheFile.exists()) cacheFile.delete();

        vm.load(); // should be a no-op since file doesn't exist
        assertEquals("Data should be unchanged after loading missing cache", 1, vm.get().size());
    }

    @Test
    public void testAddObserverWorksOnFreshViewModel() {
        // Listeners list is transient; verify addObserver works on a brand new instance
        TaskViewModel vm = new TaskViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);
        vm.setAll(List.of());

        assertEquals(1, received.size());
        assertEquals(ViewModelChangeType.TASKS, received.get(0));
    }

    // --- Data freshness ---

    @Test
    public void testObserverSeesUpdatedDataDuringNotification() {
        ProjectViewModel vm = new ProjectViewModel();
        ProjectDTO project = new ProjectDTO(1L, "Fresh", "desc", null, "owner", List.of(), 0, 0, 0);
        List<ProjectDTO> seenDuringNotification = new ArrayList<>();

        vm.addObserver(type -> {
            // During notification, get() should return the updated data
            seenDuringNotification.addAll(vm.get());
        });

        vm.setAll(List.of(project));

        assertEquals(1, seenDuringNotification.size());
        assertEquals("Fresh", seenDuringNotification.get(0).projectName());
    }
}
