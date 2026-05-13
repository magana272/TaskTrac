import task.trak.api.dto.TaskDTO;
import task.trak.api.model.Session;
import task.trak.app.client.gui.viewmodel.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class AppModelTest {

    private ViewModelChangeType lastChangeType;

    private ViewModelChangeListener trackingListener() {
        lastChangeType = null;
        return type -> lastChangeType = type;
    }

    private TaskDTO task(long id, String project, String title, String status) {
        return new TaskDTO(id, project, "user", title, status, null, null, null, null, null, 0);
    }

    private TaskDTO taskWithDeadline(long id, String title, Date deadline) {
        return new TaskDTO(id, "P", "user", title, "READY", null, null, null, deadline, null, 0);
    }

    private TaskDTO taskWithEstimate(long id, String title, String estimate) {
        return new TaskDTO(id, "P", "user", title, "READY", null, null, null, null, estimate, 0);
    }

    // --- TaskViewModel tests ---

    @Test
    public void testInitialStateIsEmpty() {
        TaskViewModel vm = new TaskViewModel();
        assertTrue(vm.get().isEmpty());
        assertFalse(vm.isShowCompleted());
        assertEquals("None", vm.getSort());
        assertEquals("All", vm.getProjectFilter());
    }

    @Test
    public void testSetTasksFiresListener() {
        TaskViewModel vm = new TaskViewModel();
        vm.addObserver(trackingListener());
        vm.setAll(List.of(task(1, "Proj", "T1", "READY")));
        assertEquals(ViewModelChangeType.TASKS, lastChangeType);
        assertEquals(1, vm.get().size());
    }

    @Test
    public void testSetShowCompletedFiresListener() {
        TaskViewModel vm = new TaskViewModel();
        vm.addObserver(trackingListener());
        vm.setShowCompleted(true);
        assertEquals(ViewModelChangeType.TASKS, lastChangeType);
        assertTrue(vm.isShowCompleted());
    }

    @Test
    public void testSetSortFiresListener() {
        TaskViewModel vm = new TaskViewModel();
        vm.addObserver(trackingListener());
        vm.setSort("Due Date");
        assertEquals(ViewModelChangeType.TASKS, lastChangeType);
        assertEquals("Due Date", vm.getSort());
    }

    @Test
    public void testSetProjectFilterFiresListener() {
        TaskViewModel vm = new TaskViewModel();
        vm.addObserver(trackingListener());
        vm.setProjectFilter("MyProject");
        assertEquals(ViewModelChangeType.TASKS, lastChangeType);
        assertEquals("MyProject", vm.getProjectFilter());
    }

    @Test
    public void testGetFilteredExcludesCompleted() {
        TaskViewModel vm = new TaskViewModel();
        vm.setAll(List.of(
                task(1, "P", "T1", "READY"),
                task(2, "P", "T2", "COMPLETE")
        ));
        vm.setShowCompleted(false);
        List<TaskDTO> filtered = vm.getFiltered();
        assertEquals(1, filtered.size());
        assertEquals("READY", filtered.get(0).status());
    }

    @Test
    public void testGetFilteredIncludesCompletedWhenEnabled() {
        TaskViewModel vm = new TaskViewModel();
        vm.setAll(List.of(
                task(1, "P", "T1", "READY"),
                task(2, "P", "T2", "COMPLETE")
        ));
        vm.setShowCompleted(true);
        List<TaskDTO> filtered = vm.getFiltered();
        assertEquals(2, filtered.size());
    }

    @Test
    public void testGetFilteredSortsByDueDate() {
        Date early = new Date(1000000);
        Date late = new Date(9000000);
        TaskViewModel vm = new TaskViewModel();
        vm.setShowCompleted(true);
        vm.setAll(List.of(
                taskWithDeadline(1, "Late", late),
                taskWithDeadline(2, "Early", early)
        ));
        vm.setSort("Due Date");
        List<TaskDTO> filtered = vm.getFiltered();
        assertEquals("Early", filtered.get(0).title());
        assertEquals("Late", filtered.get(1).title());
    }

    @Test
    public void testGetFilteredSortsByEstimate() {
        TaskViewModel vm = new TaskViewModel();
        vm.setShowCompleted(true);
        vm.setAll(List.of(
                taskWithEstimate(1, "Big", "8h"),
                taskWithEstimate(2, "Small", "1h")
        ));
        vm.setSort("Estimate");
        List<TaskDTO> filtered = vm.getFiltered();
        assertEquals("Small", filtered.get(0).title());
        assertEquals("Big", filtered.get(1).title());
    }

    @Test
    public void testGetFilteredFiltersByProject() {
        TaskViewModel vm = new TaskViewModel();
        vm.setShowCompleted(true);
        vm.setAll(List.of(
                task(1, "Alpha", "T1", "READY"),
                task(2, "Beta", "T2", "READY")
        ));
        vm.setProjectFilter("Alpha");
        List<TaskDTO> filtered = vm.getFiltered();
        assertEquals(1, filtered.size());
        assertEquals("Alpha", filtered.get(0).projectName());
    }

    @Test
    public void testRemoveListenerStopsNotifications() {
        TaskViewModel vm = new TaskViewModel();
        ViewModelChangeListener listener = trackingListener();
        vm.addObserver(listener);
        vm.removeObserver(listener);
        vm.setAll(List.of());
        assertNull(lastChangeType);
    }

    @Test
    public void testMultipleListenersAllNotified() {
        TaskViewModel vm = new TaskViewModel();
        List<ViewModelChangeType> received = new ArrayList<>();
        vm.addObserver(received::add);
        vm.addObserver(received::add);
        vm.setAll(List.of());
        assertEquals(2, received.size());
    }

    @Test
    public void testSetNullTasksDefaultsToEmptyList() {
        TaskViewModel vm = new TaskViewModel();
        vm.setAll(null);
        assertNotNull(vm.get());
        assertTrue(vm.get().isEmpty());
    }

    // --- ProjectViewModel tests ---

    @Test
    public void testProjectViewModelSetAll() {
        ProjectViewModel vm = new ProjectViewModel();
        vm.addObserver(trackingListener());
        vm.setAll(List.of());
        assertEquals(ViewModelChangeType.PROJECTS, lastChangeType);
    }

    // --- SprintViewModel tests ---

    @Test
    public void testSprintViewModelSetAll() {
        SprintViewModel vm = new SprintViewModel();
        vm.addObserver(trackingListener());
        vm.setAll(List.of());
        assertEquals(ViewModelChangeType.SPRINTS, lastChangeType);
    }

    // --- UserViewModel tests ---

    @Test
    public void testUserViewModelSetSession() {
        UserViewModel vm = new UserViewModel();
        vm.addObserver(trackingListener());
        vm.setSession(new Session("testuser"));
        assertEquals(ViewModelChangeType.SESSION, lastChangeType);
        assertEquals("testuser", vm.getSession().getLogged_in_user());
    }

    @Test
    public void testUserViewModelSetError() {
        UserViewModel vm = new UserViewModel();
        vm.addObserver(trackingListener());
        vm.setError("Something went wrong");
        assertEquals(ViewModelChangeType.ERROR, lastChangeType);
        assertEquals("Something went wrong", vm.getLastError());
    }
}
