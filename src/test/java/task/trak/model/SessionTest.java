package task.trak.model;

import task.trak.model.Session;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.SessionDAO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class SessionTest {

    private static final String TEST_STORE = ".store_session_test";
    private String originalStoreDir;

    @Before
    public void setUp() {
        originalStoreDir = TTApp.storedir;
        TTApp.storedir = TEST_STORE;
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
    }

    @Test
    public void TestCreateSession() {
        Session session = new Session("manuel");
        assertEquals("manuel", session.getLogged_in_user());
        assertNull(session.getCurrent_task_id());
        assertNull(session.getTask_started_at());
    }

    @Test
    public void TestSetCurrentTask() {
        Session session = new Session("manuel");
        session.setCurrent_task_id(12345L);
        session.setTask_started_at(System.currentTimeMillis());
        assertEquals(Long.valueOf(12345L), session.getCurrent_task_id());
        assertNotNull(session.getTask_started_at());
    }

    @Test
    public void TestSaveAndLoad() {
        Session session = new Session("alice");
        session.setCurrent_task_id(99L);
        SessionDAO.save(session);

        Session loaded = SessionDAO.load();
        assertNotNull(loaded);
        assertEquals("alice", loaded.getLogged_in_user());
        assertEquals(Long.valueOf(99L), loaded.getCurrent_task_id());
    }

    @Test
    public void TestLoadReturnsNullWhenNoSession() {
        Session loaded = SessionDAO.load();
        assertNull(loaded);
    }

    @Test
    public void TestClear() {
        Session session = new Session("bob");
        SessionDAO.save(session);
        assertNotNull(SessionDAO.load());

        SessionDAO.clear();
        assertNull(SessionDAO.load());
    }

    @Test
    public void TestClearWhenNoSession() {
        SessionDAO.clear();
        assertNull(SessionDAO.load());
    }
}
