package task.trak.app.server.service.task;

import task.trak.api.dto.TaskDTO;
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
    public TaskDTO create(String title, String projectName, String assignedTo, String summary, Date deadline, String estimate) {
        Long id = System.currentTimeMillis();
        Task task = new Task(id, projectName, assignedTo, title, STATE.READY, null, null, summary);
        if (deadline != null) task.setDeadline(deadline);
        if (estimate != null) task.setEstimate(estimate);
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
    public TaskDTO updateById(Long id, String newTitle, String newStatus, String newAssignedTo, String newSummary, String newEstimate) {
        Task task = store.loadByKey(String.valueOf(id));
        if (task == null) {
            throw new IllegalArgumentException("Task " + id + " not found.");
        }

        if (newTitle != null) {
            task.setTitle(newTitle);
        }
        if (newStatus != null) {
            STATE status = STATE.valueOf(newStatus.toUpperCase());
            task.setStatus(status);
            if (status == STATE.COMPLETE) {
                task.setCompleted_at(new Date());
            }
        }
        if (newAssignedTo != null) {
            task.setAssigned_to(newAssignedTo);
        }
        if (newSummary != null) {
            task.setSummary(newSummary);
        }
        if (newEstimate != null) {
            task.setEstimate(newEstimate);
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
