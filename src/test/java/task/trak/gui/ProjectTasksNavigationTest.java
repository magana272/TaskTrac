package task.trak.gui;

import task.trak.model.dto.TaskDTO;
import task.trak.app.client.gui.viewmodel.TaskViewModel;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ProjectTasksNavigationTest {

    @Test
    public void testProjectFilterSetCorrectly() {
        TaskViewModel vm = new TaskViewModel();
        assertEquals("All", vm.getProjectFilter());

        vm.setProjectFilter("MyProject");
        assertEquals("MyProject", vm.getProjectFilter());
    }

    @Test
    public void testFilteredTasksRespectProjectFilter() {
        TaskViewModel vm = new TaskViewModel();
        vm.setAll(List.of(
                new TaskDTO(1L, "ProjectA", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null),
                new TaskDTO(2L, "ProjectB", "u", "T2", "READY", null, null, null, null, null, 0, 0, 0, null),
                new TaskDTO(3L, "ProjectA", "u", "T3", "READY", null, null, null, null, null, 0, 0, 0, null)
        ));

        vm.setProjectFilter("ProjectA");
        List<TaskDTO> filtered = vm.getFiltered();

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(t -> "ProjectA".equals(t.projectName())));
    }

    @Test
    public void testFilterAllShowsAllTasks() {
        TaskViewModel vm = new TaskViewModel();
        vm.setAll(List.of(
                new TaskDTO(1L, "ProjectA", "u", "T1", "READY", null, null, null, null, null, 0, 0, 0, null),
                new TaskDTO(2L, "ProjectB", "u", "T2", "READY", null, null, null, null, null, 0, 0, 0, null)
        ));

        vm.setProjectFilter("All");
        List<TaskDTO> filtered = vm.getFiltered();

        assertEquals(2, filtered.size());
    }

    @Test
    public void testSetProjectFilterNotifiesObservers() {
        TaskViewModel vm = new TaskViewModel();
        java.util.ArrayList<task.trak.app.client.gui.viewmodel.ViewModelChangeType> received = new java.util.ArrayList<>();
        vm.addObserver(received::add);

        vm.setProjectFilter("ProjectA");

        assertTrue(received.contains(task.trak.app.client.gui.viewmodel.ViewModelChangeType.TASKS));
    }
}
