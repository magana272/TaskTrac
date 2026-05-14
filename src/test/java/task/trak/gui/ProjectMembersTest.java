package task.trak.gui;

import task.trak.model.dto.ProjectDTO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProjectMembersTest {

    /** Simulates the fixed getProjectMembers logic */
    private List<String> buildMemberList(ProjectDTO project) {
        List<String> result = new ArrayList<>();
        if (project.ownerUsername() != null) {
            result.add(project.ownerUsername());
        }
        if (project.memberUsernames() != null) {
            for (String m : project.memberUsernames()) {
                if (!m.equals(project.ownerUsername())) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    @Test
    public void testOwnerNotInMemberUsernames() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner1", List.of("member1", "member2"), 2, 0, 0);
        assertFalse(project.memberUsernames().contains("owner1"));
    }

    @Test
    public void testCombinedListIncludesOwnerFirst() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner1", List.of("member1", "member2"), 2, 0, 0);
        List<String> combined = buildMemberList(project);

        assertEquals(3, combined.size());
        assertEquals("owner1", combined.get(0));
        assertTrue(combined.contains("member1"));
        assertTrue(combined.contains("member2"));
    }

    @Test
    public void testNoDuplicatesWhenOwnerIsMember() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner1", List.of("owner1", "member1"), 2, 0, 0);
        List<String> combined = buildMemberList(project);

        assertEquals(2, combined.size());
        assertEquals("owner1", combined.get(0));
        assertEquals("member1", combined.get(1));
    }

    @Test
    public void testNullOwner() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                null, List.of("member1"), 1, 0, 0);
        List<String> combined = buildMemberList(project);

        assertEquals(1, combined.size());
        assertEquals("member1", combined.get(0));
    }

    @Test
    public void testNullMembers() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner1", null, 0, 0, 0);
        List<String> combined = buildMemberList(project);

        assertEquals(1, combined.size());
        assertEquals("owner1", combined.get(0));
    }
}
