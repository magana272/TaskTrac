package task.trak.api.service;

import task.trak.api.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {
    ProjectDTO create(String name);

    ProjectDTO create(String name, String summary, String ownerUsername, List<String> memberUsernames);

    ProjectDTO getById(Long id);

    ProjectDTO getByName(String name);

    boolean deleteByName(String name);

    ProjectDTO updateByName(String projectName, String newName, String newSummary, List<String> newMemberUsernames);

    List<ProjectDTO> listAll();

    List<ProjectDTO> listByUser(String username);

    ProjectDTO addMember(String projectName, String username);
}
