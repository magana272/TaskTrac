package task.trak.cli;

import task.trak.Main;
import task.trak.app.client.cli.TTApp;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class CMDTest {

    @Before
    public void setUp() {
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) {
            storeDir.mkdirs();
        }
    }

    @Test
    public void testCreateProject() {
        // Create a user first, then create a project with that user as owner
        Main.main(new String[]{"user", "add", "testowner", "--first_name", "Test", "--last_name", "Owner"});
        Main.main(new String[]{"project", "add", "Project_1", "--owner", "testowner"});
    }
}
