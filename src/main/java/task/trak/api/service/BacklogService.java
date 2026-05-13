package task.trak.api.service;

import task.trak.api.dto.BacklogDTO;

public interface BacklogService {
    BacklogDTO create(String name, String projectName);

    BacklogDTO getByName(String name);

    boolean deleteByName(String name);

    BacklogDTO addTask(String backlogName, Long taskId);

    BacklogDTO removeTask(String backlogName, Long taskId);
}
