package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

import java.util.List;

public record UpdateProjectRequest(
        String projectName,
        String newName,
        String newSummary,
        List<String> newMemberUsernames
) {
    public void validate() {
        if (projectName == null || projectName.isBlank()) throw new ValidationException("Project name is required.");
    }
}
