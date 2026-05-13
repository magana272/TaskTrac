import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.project.TrakProjectBuilder;
import task.trak.app.server.model.user.TrakBuilderUser;
import task.trak.app.server.model.user.User;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class ProjectTest {
    private final Project project_with_initally_10_members =
            new TrakProjectBuilder().
                    setMembers(createDummyUser(10)).
                    setProjectName("project_with_initally_10_members").
                    build();
    private Project test_project;
    private User test_owner;

    private static List<User> createDummyUser(int num) {
        List<User> res = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            res.add(
                    new TrakBuilderUser().
                            setFirstName("First").
                            setLastName(String.valueOf(i)).
                            setID(Long.valueOf(i)).
                            build()
            );
        }
        return res;
    }

    @Before
    public void setUp() {
        this.test_owner = new TrakBuilderUser()
                .setFirstName("Manuel")
                .setLastName("Magana")
                .setID(Long.valueOf(1))
                .build();
        String project_name = "Project Name";
        this.test_project = new TrakProjectBuilder().
                setProjectName(project_name).
                setOwner(test_owner).
                setMembers(createDummyUser(0)).
                build();

    }

    @Test
    public void TestCreate() {
        User owner = new TrakBuilderUser()
                .setFirstName("Manuel")
                .setLastName("Magana")
                .setID(Long.valueOf(1))
                .build();
        String project_name = "Projesct Name";
        Project project = new TrakProjectBuilder().
                setOwner(owner).
                setProjectName(project_name).build();
        assertNotNull("Project should have been created", project);
        assertNotNull("Project should have owner", project.getOwner());
        assertNull(project.getMembers());
        assertEquals(project.getName(), project_name);
    }

    @Test
    public void TestAddMember() {

        assertNotNull(
                "TestAddMember: this.test_project should be initialized",
                this.
                        test_project);
        assertNotNull("TestAddMember: Members should be initialized",
                this.
                        test_project.
                        getMembers()
        );

        assertEquals("TestAddMember: Member List Is not Empty",
                this.test_project.getMembers().size(), 0);
        for (int i = 0; i < 10; i++) {
            this.
                    test_project.
                    add(
                            new TrakBuilderUser()
                                    .setFirstName("First")
                                    .setLastName(Integer.valueOf(i).toString())
                                    .setID(Long.valueOf(i))
                                    .build());
        }
        List<User> users = this.test_project.getMembers();
        Integer user_count = Integer.valueOf(Math.toIntExact(users.stream().count()));
        assertEquals("Should be 10 users but actual users == " + user_count, 10, (int) user_count);
    }

    @Test
    public void TestDeleteMember() {

        assertEquals(this.project_with_initally_10_members.getMembers().size(), 10);
        int i = 10;
        while (i != 0) {
            this.test_project.removeMemberByID(Long.valueOf(i));
            i--;
        }
        assertTrue(this.test_project.getMembers().isEmpty());
    }

    @Test
    public void TestNumberOfTaskAndNumberOfNumbers() {
        Integer num_members = this.project_with_initally_10_members.getNumberOfMembers();
        Integer num_sprints = this.project_with_initally_10_members.getNumberOfSprints();
        Integer num_tasks = this.project_with_initally_10_members.getNumberOfTasks();
        assertEquals("Expeted 10; got: " + num_members.toString(), num_members, Integer.valueOf(10));
        assertEquals("Expected 0; got: " + num_tasks.toString(), num_tasks, Integer.valueOf(0));
        assertEquals("Expected 0; got: " + num_sprints.toString(), num_sprints, Integer.valueOf(0));
    }

    @Test
    public void TestCreateWithSummary() {
        Project project = new TrakProjectBuilder()
                .setProjectName("SummaryTest")
                .setSummary("A test summary")
                .build();
        assertNotNull(project);
        assertEquals("A test summary", project.getSummary());
    }

    @Test
    public void TestSummaryDefaultsToNull() {
        Project project = new TrakProjectBuilder()
                .setProjectName("NoSummary")
                .build();
        assertNull(project.getSummary());
    }

}
