package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

import java.util.List;

public record CreateProjectRequest(
        String name,
        String summary,
        String ownerUsername,
        List<String> memberUsernames
) {
    public void validate() {
        if (name == null || name.isBlank()) throw new ValidationException("Project name is required.");
    }
}
