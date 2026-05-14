package task.trak.model;

import task.trak.app.server.model.sprint.Sprint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class SprintTest {

    @Test
    public void TestCreateWithAllFields() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 14L * 24 * 60 * 60 * 1000);
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(1L);
        taskIds.add(2L);
        Sprint sprint = new Sprint(1L, "MyProject", "Sprint 1", taskIds, start, end);
        assertNotNull(sprint);
        assertEquals(Long.valueOf(1L), sprint.getId());
        assertEquals("MyProject", sprint.getProject_name());
        assertEquals("Sprint 1", sprint.getName());
        assertEquals(2, sprint.getTask_ids().size());
        assertEquals(start, sprint.getStart_date());
        assertEquals(end, sprint.getEnd_date());
    }

    @Test
    public void TestTaskIdsDefaultToEmptyList() {
        Sprint sprint = new Sprint(2L, "Proj", "Sprint 2", null, null, null);
        assertNotNull(sprint.getTask_ids());
        assertTrue(sprint.getTask_ids().isEmpty());
    }

    @Test
    public void TestDatesDefaultToNull() {
        Sprint sprint = new Sprint(3L, "Proj", "Sprint 3", null, null, null);
        assertNull(sprint.getStart_date());
        assertNull(sprint.getEnd_date());
    }

    @Test
    public void TestSetName() {
        Sprint sprint = new Sprint(4L, "Proj", "Original", null, null, null);
        sprint.setName("Renamed");
        assertEquals("Renamed", sprint.getName());
    }

    @Test
    public void TestSetDates() {
        Sprint sprint = new Sprint(5L, "Proj", "Sprint", null, null, null);
        Date start = new Date();
        Date end = new Date();
        sprint.setStart_date(start);
        sprint.setEnd_date(end);
        assertEquals(start, sprint.getStart_date());
        assertEquals(end, sprint.getEnd_date());
    }

    @Test
    public void TestSetTaskIds() {
        Sprint sprint = new Sprint(6L, "Proj", "Sprint", null, null, null);
        List<Long> tasks = new ArrayList<>();
        tasks.add(10L);
        tasks.add(20L);
        tasks.add(30L);
        sprint.setTask_ids(tasks);
        assertEquals(3, sprint.getTask_ids().size());
    }
}
