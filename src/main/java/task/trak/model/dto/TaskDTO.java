package task.trak.model.dto;

import java.util.Date;

public record TaskDTO(
        Long id,
        String projectName,
        String assignedTo,
        String title,
        String status,
        Date createdAt,
        Date completedAt,
        String summary,
        Date deadline,
        String estimate,
        long timeSpentMs,
        long timeInReadyMs,
        long timeInProgressMs,
        String completionNote
) {
}
