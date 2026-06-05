package task.trak.model.dto;

import java.util.Date;
import java.util.List;

public record SprintDTO(
        Long id,
        String projectName,
        String name,
        List<Long> taskIds,
        Date startDate,
        Date endDate,
        boolean completed
) {
}
