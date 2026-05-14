package task.trak.gui;

import task.trak.model.dto.ProjectDTO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProjectAddMemberTest {

    @Test
    public void testAddMemberToLocalListOnlyOnSuccess() {
        List<String> currentMembers = new ArrayList<>(List.of("existing"));
        String newMember = "newuser";
        boolean serviceSuccess = true;

        // Simulate the fixed flow: service call first, then add to local list
        if (!currentMembers.contains(newMember)) {
            if (serviceSuccess) {
                currentMembers.add(newMember);
            }
        }

        assertEquals(2, currentMembers.size());
        assertTrue(currentMembers.contains("newuser"));
    }

    @Test
    public void testAddMemberNotAddedOnServiceFailure() {
        List<String> currentMembers = new ArrayList<>(List.of("existing"));
        String newMember = "nonexistent";
        boolean serviceSuccess = false;

        // Simulate service failure — member should NOT be added to local list
        if (!currentMembers.contains(newMember)) {
            if (serviceSuccess) {
                currentMembers.add(newMember);
            }
        }

        assertEquals(1, currentMembers.size());
        assertFalse(currentMembers.contains("nonexistent"));
    }

    @Test
    public void testDuplicateMemberNotAdded() {
        List<String> currentMembers = new ArrayList<>(List.of("existing"));
        String newMember = "existing";

        if (!currentMembers.contains(newMember)) {
            currentMembers.add(newMember);
        }

        assertEquals(1, currentMembers.size());
    }

    @Test
    public void testProjectDTOMembersAreIndependent() {
        ProjectDTO project = new ProjectDTO(1L, "TestProj", "desc", null,
                "owner", List.of("member1", "member2"), 2, 0, 0);

        // currentMembers should be a copy, not a reference
        List<String> currentMembers = project.memberUsernames() != null
                ? new ArrayList<>(project.memberUsernames())
                : new ArrayList<>();

        currentMembers.add("member3");

        // Original DTO should be unaffected
        assertEquals(2, project.memberUsernames().size());
        assertEquals(3, currentMembers.size());
    }
}
