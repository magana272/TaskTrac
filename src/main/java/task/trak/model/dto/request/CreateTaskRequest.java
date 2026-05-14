package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

import java.util.Date;

public record CreateTaskRequest(
        String title,
        String projectName,
        String assignedTo,
        String summary,
        Date deadline,
        String estimate
) {
    public void validate() {
        if (title == null || title.isBlank()) throw new ValidationException("Task title is required.");
        if (projectName == null || projectName.isBlank()) throw new ValidationException("Project name is required.");
    }
}
