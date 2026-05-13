package task.trak.app.client.gui.controller;

import task.trak.api.dto.SprintDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.gui.viewmodel.SprintViewModel;

import java.util.ArrayList;
import java.util.List;

public class SprintController {

    private final SprintViewModel sprintViewModel;

    public SprintController(SprintViewModel sprintViewModel) {
        this.sprintViewModel = sprintViewModel;
    }

    public SprintViewModel getViewModel() {
        return sprintViewModel;
    }

    public void addSprint(String name, String project, String startDate, String endDate, List<Long> taskIds) {
        SprintDTO sprint = ServiceFactory.sprintService().create(name, project);
        if (startDate != null || endDate != null) {
            ServiceFactory.sprintService().updateByNameAndProject(name, project, startDate, endDate);
        }
        if (taskIds != null && !taskIds.isEmpty()) {
            ServiceFactory.sprintService().updateTaskIds(String.valueOf(sprint.id()), taskIds);
        }
        refreshSprints();
    }

    public void updateSprint(String id, String startDate, String endDate) {
        ServiceFactory.sprintService().updateByName(id, startDate, endDate);
        refreshSprints();
    }

    public void deleteSprint(String name) {
        ServiceFactory.sprintService().deleteByName(name);
        refreshSprints();
    }

    public void addTaskToSprint(String sprintName, String project, long taskId) {
        SprintDTO sprint = ServiceFactory.sprintService().getByNameAndProject(sprintName, project);
        List<Long> taskIds = new ArrayList<>(sprint.taskIds());
        if (!taskIds.contains(taskId)) {
            taskIds.add(taskId);
        }
        ServiceFactory.sprintService().updateTaskIds(String.valueOf(sprint.id()), taskIds);
        refreshSprints();
    }

    public void refreshSprints() {
        List<SprintDTO> sprints = ServiceFactory.sprintService().listAll();
        sprintViewModel.setAll(sprints);
    }
}
