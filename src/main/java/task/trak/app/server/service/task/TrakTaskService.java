package task.trak.app.server.service.task;

import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.CreateTaskRequest;
import task.trak.model.dto.request.UpdateTaskRequest;
import task.trak.model.exception.EntityNotFoundException;
import task.trak.api.service.STATE;
import task.trak.api.service.TaskService;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TrakTaskService implements TaskService {

    private final EntityDAO<Task> store = DAOFactory.taskDAO();

    @Override
    public TaskDTO create(CreateTaskRequest request) {
        request.validate();
        Long id = System.currentTimeMillis();
        Task task = new Task(id, request.projectName(), request.assignedTo(), request.title(), STATE.READY, null, null, request.summary());
        if (request.deadline() != null) task.setDeadline(request.deadline());
        if (request.estimate() != null) task.setEstimate(request.estimate());
        store.save(task);
        return toDTO(task);
    }

    @Override
    public TaskDTO getById(Long id) {
        Task task = store.loadByKey(String.valueOf(id));
        return task != null ? toDTO(task) : null;
    }

    @Override
    public boolean deleteById(Long id) {
        return store.deleteByKey(String.valueOf(id));
    }

    @Override
    public TaskDTO updateById(UpdateTaskRequest request) {
        request.validate();
        Task task = store.loadByKey(String.valueOf(request.id()));
        if (task == null) {
            throw new EntityNotFoundException("Task " + request.id() + " not found.");
        }

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.status() != null) {
            STATE oldStatus = task.getStatus();
            STATE newStatus = STATE.valueOf(request.status().toUpperCase());

            // Leaving INPROGRESS: accumulate elapsed time
            if (oldStatus == STATE.INPROGRESS && newStatus != STATE.INPROGRESS) {
                long accumulated = task.getTime_spent_ms() != null ? task.getTime_spent_ms() : 0;
                if (task.getTime_started() != null) {
                    accumulated += System.currentTimeMillis() - task.getTime_started();
                }
                task.setTime_spent_ms(accumulated);
                task.setTime_started(null);
            }

            // Entering INPROGRESS: start timer
            if (newStatus == STATE.INPROGRESS && oldStatus != STATE.INPROGRESS) {
                task.setTime_started(System.currentTimeMillis());
            }

            task.setStatus(newStatus);
            if (newStatus == STATE.COMPLETE) {
                task.setCompleted_at(new Date());
            }
        }
        if (request.assignedTo() != null) {
            task.setAssigned_to(request.assignedTo());
        }
        if (request.summary() != null) {
            task.setSummary(request.summary());
        }
        if (request.estimate() != null) {
            task.setEstimate(request.estimate());
        }

        store.save(task);
        return toDTO(task);
    }

    @Override
    public List<TaskDTO> listAll() {
        return store.loadAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> listByAssignee(String username) {
        return store.loadAll().stream()
                .filter(t -> username.equals(t.getAssigned_to()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private TaskDTO toDTO(Task t) {
        return new TaskDTO(t.getId(), t.getProject_name(), t.getAssigned_to(), t.getTitle(),
                t.getStatus().name(), t.getCreated_at(), t.getCompleted_at(), t.getSummary(),
                t.getDeadline(), t.getEstimate(), t.getElapsedMs());
    }
}
