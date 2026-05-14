package task.trak.app.client.gui.controller;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.request.CreateProjectRequest;
import task.trak.model.dto.request.UpdateProjectRequest;
import task.trak.app.client.http.ProjectHttpService;
import task.trak.app.client.gui.viewmodel.ProjectViewModel;
import task.trak.app.client.gui.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectController {

    private final ProjectHttpService projectService;
    private final ProjectViewModel projectViewModel;
    private final UserViewModel userViewModel;

    public ProjectController(ProjectHttpService projectService, ProjectViewModel projectViewModel, UserViewModel userViewModel) {
        this.projectService = projectService;
        this.projectViewModel = projectViewModel;
        this.userViewModel = userViewModel;
    }

    public ProjectViewModel getViewModel() {
        return projectViewModel;
    }

    public void addProject(String name, String summary) {
        String owner = userViewModel.getSession() != null
                ? userViewModel.getSession().getLogged_in_user()
                : "guest";
        this.projectService.create(new CreateProjectRequest(name, summary, owner, new ArrayList<>()));
        refreshProjects();
    }

    public void updateProject(String name, String newName, String summary) {
        this.projectService.updateByName(new UpdateProjectRequest(name, newName, summary, null));
        refreshProjects();
    }

    public void deleteProject(String name) {
        this.projectService.deleteByName(name);
        refreshProjects();
    }

    public void addMember(String projectName, String username) {
        this.projectService.addMember(projectName, username);
        refreshProjects();
    }

    public void removeMember(String projectName, List<String> remainingMembers) {
        this.projectService.updateByName(new UpdateProjectRequest(projectName, null, null, remainingMembers));
        refreshProjects();
    }

    public void refreshProjects() {
        String user = userViewModel.getSession() != null
                ? userViewModel.getSession().getLogged_in_user() : null;
        List<ProjectDTO> projects = user != null
                ? this.projectService.listByUser(user)
                : this.projectService.listAll();
        projectViewModel.setAll(projects);
    }

    public List<ProjectDTO> getProjectsForUser(String username) {
        return this.projectService.listByUser(username);
    }

    public List<String> getProjectMembers(String projectName) {
        ProjectDTO project = this.projectService.getByName(projectName);
        if (project == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        if (project.ownerUsername() != null) {
            result.add(project.ownerUsername());
        }
        if (project.memberUsernames() != null) {
            for (String m : project.memberUsernames()) {
                if (!m.equals(project.ownerUsername())) {
                    result.add(m);
                }
            }
        }
        return result;
    }
}
