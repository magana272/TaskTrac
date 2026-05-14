package task.trak.api.dto.request;

import task.trak.api.exception.ValidationException;

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
