package task.trak.app.server.service.backlog;

import task.trak.api.dto.BacklogDTO;
import task.trak.api.dto.request.CreateBacklogRequest;
import task.trak.api.exception.EntityNotFoundException;
import task.trak.api.service.BacklogService;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;

public class TrakBacklogService implements BacklogService {

    private final EntityDAO<BackLog> store = DAOFactory.backlogDAO();

    @Override
    public BacklogDTO create(CreateBacklogRequest request) {
        request.validate();
        Long id = System.currentTimeMillis();
        BackLog backlog = new BackLog(id, request.name(), request.projectName(), null, null);
        store.save(backlog);
        return toDTO(backlog);
    }

    @Override
    public BacklogDTO getByName(String name) {
        BackLog backlog = store.loadByKey(name);
        return backlog != null ? toDTO(backlog) : null;
    }

    @Override
    public boolean deleteByName(String name) {
        return store.deleteByKey(name);
    }

    @Override
    public BacklogDTO addTask(String backlogName, Long taskId) {
        BackLog backlog = store.loadByKey(backlogName);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog \"" + backlogName + "\" not found.");
        }
        backlog.addTask(taskId);
        store.save(backlog);
        return toDTO(backlog);
    }

    @Override
    public BacklogDTO removeTask(String backlogName, Long taskId) {
        BackLog backlog = store.loadByKey(backlogName);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog \"" + backlogName + "\" not found.");
        }
        backlog.removeTask(taskId);
        store.save(backlog);
        return toDTO(backlog);
    }

    private BacklogDTO toDTO(BackLog b) {
        return new BacklogDTO(b.getId(), b.getName(), b.getProject_name(),
                b.getTask_ids(), b.getCreated_at());
    }
}
