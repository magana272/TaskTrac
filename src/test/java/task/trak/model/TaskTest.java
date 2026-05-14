package task.trak.model;

import task.trak.api.service.STATE;
import task.trak.app.server.model.task.Task;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TaskTest {

    @Test
    public void TestCreateWithAllFields() {
        Date now = new Date();
        Task task = new Task(1L, "MyProject", "jdoe", "Fix bug", STATE.READY, now, null, "Fix the login bug");
        assertNotNull(task);
        assertEquals(Long.valueOf(1L), task.getId());
        assertEquals("MyProject", task.getProject_name());
        assertEquals("jdoe", task.getAssigned_to());
        assertEquals("Fix bug", task.getTitle());
        assertEquals(STATE.READY, task.getStatus());
        assertEquals(now, task.getCreated_at());
        assertNull(task.getCompleted_at());
        assertEquals("Fix the login bug", task.getSummary());
    }

    @Test
    public void TestDefaultStatusIsReady() {
        Task task = new Task(2L, "Proj", "user1", "Title", null, null, null, null);
        assertEquals(STATE.READY, task.getStatus());
    }

    @Test
    public void TestDefaultCreatedAtIsNotNull() {
        Task task = new Task(3L, "Proj", "user1", "Title", null, null, null, null);
        assertNotNull(task.getCreated_at());
    }

    @Test
    public void TestSetStatus() {
        Task task = new Task(4L, "Proj", "user1", "Title", STATE.READY, null, null, null);
        task.setStatus(STATE.INPROGRESS);
        assertEquals(STATE.INPROGRESS, task.getStatus());
    }

    @Test
    public void TestSetAssignedTo() {
        Task task = new Task(5L, "Proj", "user1", "Title", null, null, null, null);
        task.setAssigned_to("user2");
        assertEquals("user2", task.getAssigned_to());
    }

    @Test
    public void TestSetSummary() {
        Task task = new Task(6L, "Proj", "user1", "Title", null, null, null, null);
        task.setSummary("Updated summary");
        assertEquals("Updated summary", task.getSummary());
    }

    @Test
    public void TestSetTitle() {
        Task task = new Task(7L, "Proj", "user1", "Original", null, null, null, null);
        task.setTitle("Updated");
        assertEquals("Updated", task.getTitle());
    }

    @Test
    public void TestSetCompletedAt() {
        Task task = new Task(8L, "Proj", "user1", "Title", null, null, null, null);
        Date completed = new Date();
        task.setCompleted_at(completed);
        assertEquals(completed, task.getCompleted_at());
    }

    @Test
    public void TestDeadlineDefaultsToNull() {
        Task task = new Task(9L, "Proj", "user1", "Title", null, null, null, null);
        assertNull(task.getDeadline());
    }

    @Test
    public void TestSetDeadline() {
        Task task = new Task(10L, "Proj", "user1", "Title", null, null, null, null);
        Date deadline = new Date(System.currentTimeMillis() + 86400000);
        task.setDeadline(deadline);
        assertEquals(deadline, task.getDeadline());
    }

    @Test
    public void TestEstimateDefaultsToNull() {
        Task task = new Task(11L, "Proj", "user1", "Title", null, null, null, null);
        assertNull(task.getEstimate());
    }

    @Test
    public void TestSetEstimate() {
        Task task = new Task(12L, "Proj", "user1", "Title", null, null, null, null);
        task.setEstimate("4h");
        assertEquals("4h", task.getEstimate());
    }

    @Test
    public void TestTimeTrackingDefaults() {
        Task task = new Task(13L, "Proj", "user1", "Title", null, null, null, null);
        assertNull(task.getTime_started());
        assertNull(task.getTime_spent_ms());
        assertEquals(0, task.getElapsedMs());
    }

    @Test
    public void TestElapsedMsWithAccumulatedTime() {
        Task task = new Task(14L, "Proj", "user1", "Title", null, null, null, null);
        task.setTime_spent_ms(60000L);
        assertEquals(60000L, task.getElapsedMs());
    }

    @Test
    public void TestElapsedMsWithRunningTimer() {
        Task task = new Task(15L, "Proj", "user1", "Title", null, null, null, null);
        task.setTime_started(System.currentTimeMillis() - 5000);
        long elapsed = task.getElapsedMs();
        assertTrue("Elapsed should be ~5000ms, got: " + elapsed, elapsed >= 4000 && elapsed <= 6000);
    }

    @Test
    public void TestElapsedMsCombinesAccumulatedAndRunning() {
        Task task = new Task(16L, "Proj", "user1", "Title", null, null, null, null);
        task.setTime_spent_ms(10000L);
        task.setTime_started(System.currentTimeMillis() - 5000);
        long elapsed = task.getElapsedMs();
        assertTrue("Elapsed should be ~15000ms, got: " + elapsed, elapsed >= 14000 && elapsed <= 16000);
    }
}
