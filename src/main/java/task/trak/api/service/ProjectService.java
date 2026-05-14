package task.trak.api.service;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.request.CreateProjectRequest;
import task.trak.api.dto.request.UpdateProjectRequest;

import java.util.List;

public interface ProjectService {
    ProjectDTO create(CreateProjectRequest request);

    ProjectDTO getById(Long id);

    ProjectDTO getByName(String name);

    boolean deleteByName(String name);

    ProjectDTO updateByName(UpdateProjectRequest request);

    List<ProjectDTO> listAll();

    List<ProjectDTO> listByUser(String username);

    ProjectDTO addMember(String projectName, String username);
}
