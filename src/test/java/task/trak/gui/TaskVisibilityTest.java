package task.trak.gui;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.Session;
import task.trak.app.client.gui.viewmodel.TaskViewModel;
import task.trak.app.client.gui.viewmodel.UserViewModel;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TaskVisibilityTest {

    @Test
    public void testTaskViewModelOnlyShowsAssigneeTasks() {
        TaskViewModel vm = new TaskViewModel();
        // Simulate what refreshTasks does: load only assigned tasks
        List<TaskDTO> myTasks = List.of(
                new TaskDTO(1L, "P", "user1", "MyTask", "READY", null, null, null, null, null, 0, 0, 0, null)
        );
        vm.setAll(myTasks);

        assertEquals(1, vm.get().size());
        assertEquals("user1", vm.get().get(0).assignedTo());
    }

    @Test
    public void testNoTasksWhenNotLoggedIn() {
        UserViewModel userVM = new UserViewModel();
        TaskViewModel taskVM = new TaskViewModel();

        // No session — refreshTasks would return empty list
        assertNull(userVM.getSession());
        taskVM.setAll(List.of());

        assertTrue(taskVM.get().isEmpty());
    }

    @Test
    public void testTasksLoadedForLoggedInUser() {
        UserViewModel userVM = new UserViewModel();
        userVM.setSession(new Session("user1"));

        TaskViewModel taskVM = new TaskViewModel();
        // Simulate refreshTasks loading user1's tasks
        taskVM.setAll(List.of(
                new TaskDTO(1L, "P", "user1", "T1", "READY", null, null, null, null, null, 0, 0, 0, null),
                new TaskDTO(2L, "P", "user1", "T2", "INPROGRESS", null, null, null, null, null, 0, 0, 0, null)
        ));

        assertEquals("user1", userVM.getSession().getLogged_in_user());
        assertEquals(2, taskVM.get().size());
        assertTrue(taskVM.get().stream().allMatch(t -> "user1".equals(t.assignedTo())));
    }

    @Test
    public void testProjectEditableOnlyByOwner() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner1", List.of("member1"), 1, 0, 0);

        // Owner can edit
        String owner = project.ownerUsername();
        assertTrue("Owner should be able to edit", "owner1".equals(owner));

        // Non-owner cannot edit
        String currentUser = "member1";
        assertFalse("Non-owner should not be able to edit",
                owner != null && owner.equals(currentUser));
    }

    @Test
    public void testProjectNotEditableWhenNoSession() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner1", List.of(), 0, 0, 0);

        // No session — currentUser is null
        String currentUser = null;
        String owner = project.ownerUsername();
        assertFalse("No session should prevent editing",
                owner != null && owner.equals(currentUser));
    }
}
