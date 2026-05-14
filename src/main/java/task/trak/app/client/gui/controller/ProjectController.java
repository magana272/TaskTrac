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
        try {
            String owner = userViewModel.getSession() != null
                    ? userViewModel.getSession().getLogged_in_user()
                    : "guest";
            this.projectService.create(new CreateProjectRequest(name, summary, owner, new ArrayList<>()));
            refreshProjects();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void updateProject(String name, String newName, String summary) {
        try {
            this.projectService.updateByName(new UpdateProjectRequest(name, newName, summary, null));
            refreshProjects();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void deleteProject(String name) {
        try {
            this.projectService.deleteByName(name);
            refreshProjects();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void addMember(String projectName, String username) {
        try {
            this.projectService.addMember(projectName, username);
            refreshProjects();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void removeMember(String projectName, List<String> remainingMembers) {
        try {
            this.projectService.updateByName(new UpdateProjectRequest(projectName, null, null, remainingMembers));
            refreshProjects();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void refreshProjects() {
        try {
            String user = userViewModel.getSession() != null
                    ? userViewModel.getSession().getLogged_in_user() : null;
            List<ProjectDTO> projects = user != null
                    ? this.projectService.listByUser(user)
                    : this.projectService.listAll();
            projectViewModel.setAll(projects);
        } catch (Exception e) {
            projectViewModel.setAll(List.of());
            userViewModel.setError(e.getMessage());
        }
    }

    public List<ProjectDTO> getProjectsForUser(String username) {
        try {
            return this.projectService.listByUser(username);
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
            return List.of();
        }
    }

    public List<String> getProjectMembers(String projectName) {
        try {
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
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
            return new ArrayList<>();
        }
    }
}
