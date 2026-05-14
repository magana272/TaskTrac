package task.trak.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import task.trak.Main;
import task.trak.app.client.cli.TTApp;

import java.io.File;

public class CMDTest {

    private static final String TEST_STORE = "src/test/.store";
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
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) f.delete();
            dir.delete();
        }
        TTApp.storedir = originalStoreDir;
    }

    @Test
    public void testCreateProject() {
        // Create a user first, then create a project with that user as owner
        Main.main(new String[]{"user", "add", "testowner", "--first_name", "Test", "--last_name", "Owner"});
        Main.main(new String[]{"project", "add", "Project_1", "--owner", "testowner"});
    }
}
