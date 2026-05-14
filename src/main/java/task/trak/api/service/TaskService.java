package task.trak.api.service;

import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.CreateTaskRequest;
import task.trak.model.dto.request.UpdateTaskRequest;

import java.util.List;

public interface TaskService {
    TaskDTO create(CreateTaskRequest request);

    TaskDTO getById(Long id);

    boolean deleteById(Long id);

    TaskDTO updateById(UpdateTaskRequest request);

    List<TaskDTO> listAll();

    List<TaskDTO> listByAssignee(String username);
}
