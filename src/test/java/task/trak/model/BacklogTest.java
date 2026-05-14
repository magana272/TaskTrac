package task.trak.model;

import task.trak.app.server.model.backlog.BackLog;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class BacklogTest {

    @Test
    public void TestCreateWithAllFields() {
        Date now = new Date();
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(1L);
        taskIds.add(2L);
        BackLog backlog = new BackLog(1L, "MainBacklog", "MyProject", taskIds, now);
        assertNotNull(backlog);
        assertEquals(Long.valueOf(1L), backlog.getId());
        assertEquals("MainBacklog", backlog.getName());
        assertEquals("MyProject", backlog.getProject_name());
        assertEquals(2, backlog.getTask_ids().size());
        assertEquals(now, backlog.getCreated_at());
    }

    @Test
    public void TestTaskIdsDefaultToEmptyList() {
        BackLog backlog = new BackLog(2L, "EmptyBacklog", "Proj", null, null);
        assertNotNull(backlog.getTask_ids());
        assertTrue(backlog.getTask_ids().isEmpty());
    }

    @Test
    public void TestCreatedAtDefaultsToNow() {
        BackLog backlog = new BackLog(3L, "Backlog", "Proj", null, null);
        assertNotNull(backlog.getCreated_at());
    }

    @Test
    public void TestAddTask() {
        BackLog backlog = new BackLog(4L, "Backlog", "Proj", null, null);
        backlog.addTask(100L);
        backlog.addTask(200L);
        assertEquals(2, backlog.getTask_ids().size());
        assertEquals(Long.valueOf(100L), backlog.getTask_ids().get(0));
    }

    @Test
    public void TestRemoveTask() {
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(10L);
        taskIds.add(20L);
        BackLog backlog = new BackLog(5L, "Backlog", "Proj", taskIds, null);
        assertEquals(2, backlog.getNumberOfTask().intValue());
        backlog.removeTask(10L);
        assertEquals(1, backlog.getNumberOfTask().intValue());
        assertEquals(Long.valueOf(20L), backlog.getTask_ids().get(0));
    }

    @Test
    public void TestGetNumberOfTask() {
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(1L);
        taskIds.add(2L);
        taskIds.add(3L);
        BackLog backlog = new BackLog(6L, "Backlog", "Proj", taskIds, null);
        assertEquals(Integer.valueOf(3), backlog.getNumberOfTask());
    }

    @Test
    public void TestSetName() {
        BackLog backlog = new BackLog(7L, "Original", "Proj", null, null);
        backlog.setName("Renamed");
        assertEquals("Renamed", backlog.getName());
    }
}
