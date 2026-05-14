package task.trak.model.dto;

import java.util.Date;
import java.util.List;

public record ProjectDTO(
        Long id,
        String projectName,
        String summary,
        Date createdAt,
        String ownerUsername,
        List<String> memberUsernames,
        int memberCount,
        int taskCount,
        int sprintCount
) {
}
