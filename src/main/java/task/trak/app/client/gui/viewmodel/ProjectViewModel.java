package task.trak.app.client.gui.viewmodel;

import task.trak.model.dto.ProjectDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectViewModel extends ObservableViewModel<ProjectDTO> {

    private List<ProjectDTO> projects = new ArrayList<>();
    private Map<String, List<String>> memberCache = new HashMap<>();
    private String selectedProject = "All";

    public ProjectViewModel() {
        super("project_viewmodel.ser");
    }

    @Override
    public List<ProjectDTO> get() {
        return projects;
    }

    @Override
    public void create(ProjectDTO item) {
        projects.add(item);
        notifyObservers(ViewModelChangeType.PROJECTS);
    }

    @Override
    public void update(ProjectDTO item) {
        projects.replaceAll(p -> p.id().equals(item.id()) ? item : p);
        notifyObservers(ViewModelChangeType.PROJECTS);
    }

    @Override
    public void delete(ProjectDTO item) {
        projects.removeIf(p -> p.id().equals(item.id()));
        notifyObservers(ViewModelChangeType.PROJECTS);
    }

    public void setAll(List<ProjectDTO> projects) {
        this.projects = projects != null ? new ArrayList<>(projects) : new ArrayList<>();
        rebuildMemberCache();
        notifyObservers(ViewModelChangeType.PROJECTS);
    }

    public void setAllSilent(List<ProjectDTO> projects) {
        this.projects = projects != null ? new ArrayList<>(projects) : new ArrayList<>();
        rebuildMemberCache();
    }

    private void rebuildMemberCache() {
        this.memberCache = new HashMap<>();
        for (ProjectDTO p : this.projects) {
            List<String> members = new ArrayList<>();
            if (p.ownerUsername() != null) members.add(p.ownerUsername());
            if (p.memberUsernames() != null) {
                for (String m : p.memberUsernames()) {
                    if (!m.equals(p.ownerUsername())) members.add(m);
                }
            }
            memberCache.put(p.projectName(), members);
        }
    }

    public List<String> getMembersForProject(String projectName) {
        return memberCache.getOrDefault(projectName, List.of());
    }

    public String getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
        notifyObservers(ViewModelChangeType.PROJECTS);
    }

    @Override
    protected void loadFrom(ObservableViewModel<?> loaded) {
        if (loaded instanceof ProjectViewModel other) {
            this.projects = other.projects;
        }
    }
}
