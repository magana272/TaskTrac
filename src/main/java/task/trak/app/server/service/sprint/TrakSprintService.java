package task.trak.app.server.service.sprint;

import task.trak.api.dto.SprintDTO;
import task.trak.api.service.SprintService;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.sprint.Sprint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TrakSprintService implements SprintService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final EntityDAO<Sprint> store = DAOFactory.sprintDAO();

    @Override
    public SprintDTO create(String name, String projectName) {
        Long id = System.currentTimeMillis();
        Sprint sprint = new Sprint(id, projectName, name, null, null, null);
        store.save(sprint);
        return toDTO(sprint);
    }

    @Override
    public SprintDTO getById(Long id) {
        Sprint sprint = store.loadByKey(String.valueOf(id));
        return sprint != null ? toDTO(sprint) : null;
    }

    @Override
    public SprintDTO getByName(String name) {
        Sprint sprint = store.loadByKey(name);
        return sprint != null ? toDTO(sprint) : null;
    }

    @Override
    public SprintDTO getByNameAndProject(String name, String projectName) {
        Sprint sprint = store.loadAll().stream()
                .filter(s -> name.equals(s.getName()) && projectName.equals(s.getProject_name()))
                .findFirst()
                .orElse(null);
        return sprint != null ? toDTO(sprint) : null;
    }

    @Override
    public boolean deleteByName(String name) {
        return store.deleteByKey(name);
    }

    @Override
    public SprintDTO updateByName(String name, String newStartDate, String newEndDate) {
        Sprint sprint = store.loadByKey(name);
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint \"" + name + "\" not found.");
        }

        if (newStartDate != null) {
            sprint.setStart_date(parseDate(newStartDate));
        }
        if (newEndDate != null) {
            sprint.setEnd_date(parseDate(newEndDate));
        }

        store.save(sprint);
        return toDTO(sprint);
    }

    @Override
    public SprintDTO updateByNameAndProject(String name, String projectName, String newStartDate, String newEndDate) {
        Sprint sprint = store.loadAll().stream()
                .filter(s -> name.equals(s.getName()) && projectName.equals(s.getProject_name()))
                .findFirst()
                .orElse(null);
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint \"" + name + "\" not found in project \"" + projectName + "\".");
        }

        if (newStartDate != null) {
            sprint.setStart_date(parseDate(newStartDate));
        }
        if (newEndDate != null) {
            sprint.setEnd_date(parseDate(newEndDate));
        }

        store.save(sprint);
        return toDTO(sprint);
    }

    @Override
    public SprintDTO updateTaskIds(String name, List<Long> taskIds) {
        Sprint sprint = store.loadByKey(name);
        if (sprint == null) {
            throw new IllegalArgumentException("Sprint \"" + name + "\" not found.");
        }
        sprint.setTask_ids(taskIds);
        store.save(sprint);
        return toDTO(sprint);
    }

    @Override
    public List<SprintDTO> listAll() {
        return store.loadAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr + ". Expected yyyy-MM-dd");
        }
    }

    private SprintDTO toDTO(Sprint s) {
        return new SprintDTO(s.getId(), s.getProject_name(), s.getName(),
                s.getTask_ids(), s.getStart_date(), s.getEnd_date());
    }
}
