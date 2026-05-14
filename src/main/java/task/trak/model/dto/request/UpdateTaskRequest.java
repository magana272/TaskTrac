package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

public record UpdateTaskRequest(
        Long id,
        String title,
        String status,
        String assignedTo,
        String summary,
        String estimate
) {
    public void validate() {
        if (id == null) throw new ValidationException("Task ID is required.");
    }
}
