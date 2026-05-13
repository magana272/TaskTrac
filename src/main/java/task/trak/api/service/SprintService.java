package task.trak.api.service;

import task.trak.api.dto.SprintDTO;

import java.util.List;

public interface SprintService {
    SprintDTO create(String name, String projectName);

    SprintDTO getById(Long id);

    SprintDTO getByName(String name);

    SprintDTO getByNameAndProject(String name, String projectName);

    boolean deleteByName(String name);

    SprintDTO updateByName(String name, String newStartDate, String newEndDate);

    SprintDTO updateByNameAndProject(String name, String projectName, String newStartDate, String newEndDate);

    SprintDTO updateTaskIds(String name, List<Long> taskIds);

    List<SprintDTO> listAll();
}
