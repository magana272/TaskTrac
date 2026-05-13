package task.trak.api.service;

import task.trak.api.dto.TaskDTO;

import java.util.Date;
import java.util.List;

public interface TaskService {
    TaskDTO create(String title, String projectName, String assignedTo, String summary, Date deadline, String estimate);

    TaskDTO getById(Long id);

    boolean deleteById(Long id);

    TaskDTO updateById(Long id, String newTitle, String newStatus, String newAssignedTo, String newSummary);

    List<TaskDTO> listAll();

    List<TaskDTO> listByAssignee(String username);
}
