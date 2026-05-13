import task.trak.app.server.model.user.TrakBuilderUser;
import task.trak.app.server.model.user.User;
import task.trak.app.server.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest {

    private User test_user;

    @Before
    public void setUp() {
        this.test_user = new TrakBuilderUser()
                .setFirstName("Manuel")
                .setLastName("Magana")
                .setID(1L)
                .setUserName("mmagana")
                .setEmail("manuel@example.com")
                .build();
    }

    @Test
    public void TestCreateWithAllFields() {
        assertNotNull(test_user);
        assertEquals("Manuel", test_user.getFirst_name());
        assertEquals("Magana", test_user.getLast_name());
        assertEquals("mmagana", test_user.getUser_name());
        assertEquals("manuel@example.com", test_user.getEmail());
    }

    @Test
    public void TestCreateWithMinimalFields() {
        User user = new TrakBuilderUser()
                .setFirstName("Jane")
                .setLastName("Doe")
                .build();
        assertNotNull(user);
        assertEquals("Jane", user.getFirst_name());
        assertEquals("Doe", user.getLast_name());
        assertNull(user.getUser_name());
        assertNull(user.getEmail());
        assertNull(user.getID());
    }

    @Test
    public void TestGetName() {
        assertEquals("ManuelMagana", test_user.getName());
    }

    @Test
    public void TestTasksDefaultToEmptyList() {
        assertNotNull(test_user.getTasks());
        assertTrue(test_user.getTasks().isEmpty());
    }

    @Test
    public void TestProjectsDefaultToEmptyList() {
        assertNotNull(test_user.getProjects());
        assertTrue(test_user.getProjects().isEmpty());
    }

    @Test
    public void TestSetEmail() {
        test_user.setEmail("new@example.com");
        assertEquals("new@example.com", test_user.getEmail());
    }

    @Test
    public void TestSetUserName() {
        test_user.setUser_name("newusername");
        assertEquals("newusername", test_user.getUser_name());
    }

    @Test
    public void TestSetTasks() {
        List<Long> tasks = new ArrayList<>();
        tasks.add(100L);
        tasks.add(200L);
        test_user.setTasks(tasks);
        assertEquals(2, test_user.getTasks().size());
        assertEquals(Long.valueOf(100L), test_user.getTasks().get(0));
    }

    @Test
    public void TestSetProjects() {
        List<Long> projects = new ArrayList<>();
        projects.add(10L);
        test_user.setProjects(projects);
        assertEquals(1, test_user.getProjects().size());
        assertEquals(Long.valueOf(10L), test_user.getProjects().get(0));
    }

    @Test
    public void TestPasswordHashNullByDefault() {
        assertNull(test_user.getPassword_hash());
    }

    @Test
    public void TestBuilderSetsPasswordHash() {
        User user = new TrakBuilderUser()
                .setFirstName("Test")
                .setLastName("User")
                .setPassword("secret123")
                .build();
        assertNotNull(user.getPassword_hash());
        assertEquals(PasswordUtil.hash("secret123"), user.getPassword_hash());
    }

    @Test
    public void TestSetPasswordHash() {
        String hash = PasswordUtil.hash("newpass");
        test_user.setPassword_hash(hash);
        assertEquals(hash, test_user.getPassword_hash());
    }
}
