package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

import java.util.List;

public record UpdateSprintRequest(
        String name,
        String projectName,
        String startDate,
        String endDate,
        List<Long> taskIds,
        Boolean completed,
        String review
) {
    public void validate() {
        if (name == null || name.isBlank()) throw new ValidationException("Sprint name is required.");
        if (completed != null && completed) {
            if (review == null || review.isBlank()) throw new ValidationException("Review is required when completing a sprint.");
            if (review.length() > 300) throw new ValidationException("Review must be 300 characters or less.");
        }
        if (review != null && review.length() > 300) throw new ValidationException("Review must be 300 characters or less.");
    }
}
