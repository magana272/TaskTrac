package task.trak.app.client.gui.controller;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.gui.viewmodel.ProjectViewModel;
import task.trak.app.client.gui.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectController {

    private final ProjectViewModel projectViewModel;
    private final UserViewModel userViewModel;

    public ProjectController(ProjectViewModel projectViewModel, UserViewModel userViewModel) {
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
        ServiceFactory.projectService().create(name, summary, owner, new ArrayList<>());
        refreshProjects();
    }

    public void updateProject(String name, String newName, String summary) {
        ServiceFactory.projectService().updateByName(name, newName, summary, null);
        refreshProjects();
    }

    public void deleteProject(String name) {
        ServiceFactory.projectService().deleteByName(name);
        refreshProjects();
    }

    public void addMember(String projectName, String username) {
        ServiceFactory.projectService().addMember(projectName, username);
        refreshProjects();
    }

    public void removeMember(String projectName, List<String> remainingMembers) {
        ServiceFactory.projectService().updateByName(projectName, null, null, remainingMembers);
        refreshProjects();
    }

    public void refreshProjects() {
        List<ProjectDTO> projects = ServiceFactory.projectService().listAll();
        projectViewModel.setAll(projects);
    }

    public List<ProjectDTO> getProjectsForUser(String username) {
        return ServiceFactory.projectService().listByUser(username);
    }

    public List<String> getProjectMembers(String projectName) {
        ProjectDTO project = ServiceFactory.projectService().getByName(projectName);
        return project != null ? project.memberUsernames() : new ArrayList<>();
    }
}
