package task.trak.app.client.gui.viewmodel;

import task.trak.model.dto.ProjectDTO;

import java.util.ArrayList;
import java.util.List;

public class ProjectViewModel extends ObservableViewModel<ProjectDTO> {

    private List<ProjectDTO> projects = new ArrayList<>();
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
        notifyObservers(ViewModelChangeType.PROJECTS);
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
