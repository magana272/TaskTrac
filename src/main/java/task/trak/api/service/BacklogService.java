package task.trak.api.service;

import task.trak.model.dto.BacklogDTO;
import task.trak.model.dto.request.CreateBacklogRequest;

public interface BacklogService {
    BacklogDTO create(CreateBacklogRequest request);

    BacklogDTO getByName(String name);

    boolean deleteByName(String name);

    BacklogDTO addTask(String backlogName, Long taskId);

    BacklogDTO removeTask(String backlogName, Long taskId);
}
