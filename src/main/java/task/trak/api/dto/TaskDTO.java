package task.trak.api.dto;

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
        long timeSpentMs
) {
}
