package task.trak.app.client.gui.controller;

import task.trak.model.dto.SprintDTO;
import task.trak.model.dto.request.CreateSprintRequest;
import task.trak.model.dto.request.UpdateSprintRequest;
import task.trak.app.client.http.SprintHttpService;
import task.trak.app.client.gui.viewmodel.SprintViewModel;

import java.util.ArrayList;
import java.util.List;

public class SprintController {

    private final SprintHttpService sprintService;
    private final SprintViewModel sprintViewModel;

    public SprintController(SprintHttpService sprintService, SprintViewModel sprintViewModel) {
        this.sprintService = sprintService;
        this.sprintViewModel = sprintViewModel;
    }

    public SprintViewModel getViewModel() {
        return sprintViewModel;
    }

    public void addSprint(String name, String project, String startDate, String endDate, List<Long> taskIds) {
        try {
            this.sprintService.create(new CreateSprintRequest(name, project));
            if (startDate != null || endDate != null || (taskIds != null && !taskIds.isEmpty())) {
                this.sprintService.update(new UpdateSprintRequest(name, project, startDate, endDate, taskIds));
            }
            refreshSprints();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void updateSprint(String name, String startDate, String endDate) {
        try {
            this.sprintService.update(new UpdateSprintRequest(name, null, startDate, endDate, null));
            refreshSprints();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void deleteSprint(String name) {
        try {
            this.sprintService.deleteByName(name);
            refreshSprints();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void addTaskToSprint(String sprintName, String project, long taskId) {
        try {
            SprintDTO sprint = this.sprintService.getByNameAndProject(sprintName, project);
            List<Long> taskIds = new ArrayList<>(sprint.taskIds());
            if (!taskIds.contains(taskId)) {
                taskIds.add(taskId);
            }
            this.sprintService.update(new UpdateSprintRequest(sprintName, project, null, null, taskIds));
            refreshSprints();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void refreshSprints() {
        try {
            List<SprintDTO> sprints = this.sprintService.listAll();
            sprintViewModel.setAll(sprints);
        } catch (Exception e) {
            sprintViewModel.setAll(List.of());
            System.err.println("Error: " + e.getMessage());
        }
    }
}
