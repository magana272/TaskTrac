package task.trak.store;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.project.TrakProjectBuilder;
import task.trak.app.server.model.user.TrakBuilderUser;
import task.trak.app.server.model.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProjectStoreJsonTest {

    private static final String TEST_STORE = ".store_test";
    private String originalStoreDir;
    private EntityDAO<Project> store;

    private DAOFactory.Format originalFormat;

    @Before
    public void setUp() {
        originalStoreDir = TTApp.storedir;
        originalFormat = DAOFactory.getFormat();
        TTApp.storedir = TEST_STORE;
        DAOFactory.setFormat(DAOFactory.Format.JSON);
        new File(TEST_STORE).mkdirs();
        store = DAOFactory.projectDAO();
    }

    @After
    public void tearDown() {
        File dir = new File(TEST_STORE);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
        }
        TTApp.storedir = originalStoreDir;
        DAOFactory.setFormat(originalFormat);
    }

    @Test
    public void testSaveCreatesJsonFile() {
        Project project = new TrakProjectBuilder()
                .setProjectName("JsonTest")
                .build();
        store.save(project);

        File jsonFile = new File(TEST_STORE + File.separator + "JsonTest.json");
        assertTrue("Expected .json file to exist", jsonFile.exists());
    }

    @Test
    public void testSavedFileIsValidJson() throws IOException {
        Project project = new TrakProjectBuilder()
                .setProjectName("ValidJson")
                .build();
        store.save(project);

        File jsonFile = new File(TEST_STORE + File.separator + "ValidJson.json");
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
            assertNotNull("File should contain valid JSON", obj);
            assertEquals("ValidJson", obj.get("project_name").getAsString());
        }
    }

    @Test
    public void testLoadByNameRoundTrip() {
        User owner = new TrakBuilderUser()
                .setFirstName("Jane")
                .setLastName("Doe")
                .setID(42L)
                .build();
        List<User> members = new ArrayList<>();
        members.add(new TrakBuilderUser()
                .setFirstName("Bob")
                .setLastName("Smith")
                .setID(99L)
                .build());

        Project original = new TrakProjectBuilder()
                .setProjectName("RoundTrip")
                .setOwner(owner)
                .setMembers(members)
                .build();
        store.save(original);

        Project loaded = store.loadByKey("RoundTrip");
        assertNotNull("Loaded project should not be null", loaded);
        assertEquals("RoundTrip", loaded.getName());
        assertNotNull("Owner should be deserialized", loaded.getOwner());
        assertEquals("JaneDoe", loaded.getOwner().getName());
        assertNotNull("Members should be deserialized", loaded.getMembers());
        assertEquals(1, loaded.getMembers().size());
        assertEquals("BobSmith", loaded.getMembers().get(0).getName());
    }

    @Test
    public void testLoadByNameReturnsNullForMissing() {
        Project result = store.loadByKey("NonExistent");
        assertNull("Should return null for missing project", result);
    }

    @Test
    public void testDeleteByNameRemovesJsonFile() {
        Project project = new TrakProjectBuilder()
                .setProjectName("ToDelete")
                .build();
        store.save(project);

        File jsonFile = new File(TEST_STORE + File.separator + "ToDelete.json");
        assertTrue("File should exist before delete", jsonFile.exists());

        boolean deleted = store.deleteByKey("ToDelete");
        assertTrue("deleteByKey should return true", deleted);
        assertFalse("File should not exist after delete", jsonFile.exists());
    }

    @Test
    public void testDeleteByNameReturnsFalseForMissing() {
        boolean deleted = store.deleteByKey("NoSuchProject");
        assertFalse("Should return false for missing project", deleted);
    }

    @Test
    public void testSummaryRoundTrip() {
        Project project = new TrakProjectBuilder()
                .setProjectName("SummaryRT")
                .setSummary("Round trip summary")
                .build();
        store.save(project);

        Project loaded = store.loadByKey("SummaryRT");
        assertNotNull(loaded);
        assertEquals("Round trip summary", loaded.getSummary());
    }
}
