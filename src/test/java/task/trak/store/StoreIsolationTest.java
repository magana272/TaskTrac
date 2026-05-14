package task.trak.store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.gui.viewmodel.ObservableViewModel;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.model.user.TrakBuilderUser;
import task.trak.app.server.model.user.User;

import java.io.File;

import static org.junit.Assert.*;

public class StoreIsolationTest {

    private static final String TEST_STORE = "src/test/.store";
    private static final String TEST_CACHE = "src/test/.cache";
    private String originalStoreDir;
    private String originalCacheDir;
    private DAOFactory.Format originalFormat;

    @Before
    public void setUp() {
        originalStoreDir = TTApp.storedir;
        originalCacheDir = ObservableViewModel.CACHE_DIR;
        originalFormat = DAOFactory.getFormat();
        TTApp.storedir = TEST_STORE;
        ObservableViewModel.CACHE_DIR = TEST_CACHE;
        DAOFactory.setFormat(DAOFactory.Format.JSON);
        new File(TEST_STORE).mkdirs();
    }

    @After
    public void tearDown() {
        for (String dir : new String[]{TEST_STORE, TEST_CACHE}) {
            File d = new File(dir);
            if (d.exists()) {
                File[] files = d.listFiles();
                if (files != null) for (File f : files) f.delete();
                d.delete();
            }
        }
        TTApp.storedir = originalStoreDir;
        ObservableViewModel.CACHE_DIR = originalCacheDir;
        DAOFactory.setFormat(originalFormat);
    }

    @Test
    public void testStoreWriteDoesNotPolluteProjectRoot() {
        User user = new TrakBuilderUser()
                .setUserName("isolationtest")
                .setFirstName("Test")
                .setLastName("User")
                .setID(1L)
                .build();
        DAOFactory.userDAO().save(user);

        assertNotNull(DAOFactory.userDAO().loadByKey("isolationtest"));

        File prodFile = new File(".store" + File.separator + "user_isolationtest.json");
        assertFalse("Test data must not leak into .store", prodFile.exists());
    }

    @Test
    public void testStoreDirIsRedirected() {
        assertEquals("src/test/.store", TTApp.storedir);
    }

    @Test
    public void testCacheDirIsRedirected() {
        assertEquals("src/test/.cache", ObservableViewModel.CACHE_DIR);
    }

    @Test
    public void testNoStaleDataBetweenTests() {
        User stale = DAOFactory.userDAO().loadByKey("isolationtest");
        assertNull("No stale data should persist between tests", stale);
    }
}
