package task.trak.model.dto;

import java.util.Date;
import java.util.List;

public record BacklogDTO(
        Long id,
        String name,
        String projectName,
        List<Long> taskIds,
        Date createdAt
) {
}
