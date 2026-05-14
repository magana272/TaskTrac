package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

public record CreateSprintRequest(
        String name,
        String projectName
) {
    public void validate() {
        if (name == null || name.isBlank()) throw new ValidationException("Sprint name is required.");
        if (projectName == null || projectName.isBlank()) throw new ValidationException("Project name is required.");
    }
}
