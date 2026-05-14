package task.trak.api.service;

import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.request.CreateSprintRequest;
import task.trak.model.dto.request.UpdateSprintRequest;

import java.util.List;

public interface SprintService {
    SprintDTO create(CreateSprintRequest request);

    SprintDTO getById(Long id);

    SprintDTO getByName(String name);

    SprintDTO getByNameAndProject(String name, String projectName);

    boolean deleteByName(String name);

    SprintDTO update(UpdateSprintRequest request);

    List<SprintDTO> listAll();
}
