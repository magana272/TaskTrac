package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

import java.util.List;

public record UpdateSprintRequest(
        String name,
        String projectName,
        String startDate,
        String endDate,
        List<Long> taskIds
) {
    public void validate() {
        if (name == null || name.isBlank()) throw new ValidationException("Sprint name is required.");
    }
}
