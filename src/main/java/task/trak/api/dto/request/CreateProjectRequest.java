package task.trak.api.dto.request;

import task.trak.api.exception.ValidationException;

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
