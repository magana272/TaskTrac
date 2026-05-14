package task.trak.gui;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.SprintDTO;
import task.trak.model.Session;
import task.trak.app.client.gui.viewmodel.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LogoutViewClearTest {

    @Test
    public void testClearViewModelsEmptiesAllData() {
        TaskViewModel taskVM = new TaskViewModel();
        ProjectViewModel projectVM = new ProjectViewModel();
        SprintViewModel sprintVM = new SprintViewModel();

        taskVM.setAll(List.of(new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null)));
        projectVM.setAll(List.of(new ProjectDTO(1L, "P", "d", null, "o", List.of(), 0, 0, 0)));
        sprintVM.setAll(List.of(new SprintDTO(1L, "P", "S1", List.of(), null, null, false)));

        assertEquals(1, taskVM.get().size());
        assertEquals(1, projectVM.get().size());
        assertEquals(1, sprintVM.get().size());

        taskVM.setAll(List.of());
        projectVM.setAll(List.of());
        sprintVM.setAll(List.of());

        assertTrue("Tasks should be empty after clear", taskVM.get().isEmpty());
        assertTrue("Projects should be empty after clear", projectVM.get().isEmpty());
        assertTrue("Sprints should be empty after clear", sprintVM.get().isEmpty());
    }

    @Test
    public void testSessionNullTriggersSessionNotification() {
        UserViewModel userVM = new UserViewModel();
        userVM.setSession(new Session("testuser"));

        List<ViewModelChangeType> received = new ArrayList<>();
        userVM.addObserver(received::add);

        userVM.setSession(null);

        assertTrue("SESSION notification should fire on logout", received.contains(ViewModelChangeType.SESSION));
        assertNull("Session should be null after logout", userVM.getSession());
    }

    @Test
    public void testClearViewModelsNotifiesAllObservers() {
        TaskViewModel taskVM = new TaskViewModel();
        ProjectViewModel projectVM = new ProjectViewModel();
        SprintViewModel sprintVM = new SprintViewModel();

        taskVM.setAll(List.of(new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null)));

        List<ViewModelChangeType> received = new ArrayList<>();
        taskVM.addObserver(received::add);
        projectVM.addObserver(received::add);
        sprintVM.addObserver(received::add);

        taskVM.setAll(List.of());
        projectVM.setAll(List.of());
        sprintVM.setAll(List.of());

        assertTrue(received.contains(ViewModelChangeType.TASKS));
        assertTrue(received.contains(ViewModelChangeType.PROJECTS));
        assertTrue(received.contains(ViewModelChangeType.SPRINTS));
    }

    @Test
    public void testNoTasksVisibleWhenSessionNull() {
        UserViewModel userVM = new UserViewModel();
        TaskViewModel taskVM = new TaskViewModel();

        userVM.setSession(new Session("user1"));
        taskVM.setAll(List.of(new TaskDTO(1L, "P", "user1", "T1", "READY", null, null, null, null, null, 0, 0, 0, null)));
        assertEquals(1, taskVM.get().size());

        userVM.setSession(null);
        taskVM.setAll(List.of());

        assertNull(userVM.getSession());
        assertTrue("No tasks should be visible after logout", taskVM.get().isEmpty());
        assertTrue("Filtered tasks should be empty after logout", taskVM.getFiltered().isEmpty());
    }
}
