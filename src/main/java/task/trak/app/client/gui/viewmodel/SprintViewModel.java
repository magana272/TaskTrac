package task.trak.app.client.gui.viewmodel;

import task.trak.model.dto.SprintDTO;

import java.util.ArrayList;
import java.util.List;

public class SprintViewModel extends ObservableViewModel<SprintDTO> {

    private List<SprintDTO> sprints = new ArrayList<>();

    public SprintViewModel() {
        super("sprint_viewmodel.ser");
    }

    @Override
    public List<SprintDTO> get() {
        return sprints;
    }

    @Override
    public void create(SprintDTO item) {
        sprints.add(item);
        notifyObservers(ViewModelChangeType.SPRINTS);
    }

    @Override
    public void update(SprintDTO item) {
        sprints.replaceAll(s -> s.id().equals(item.id()) ? item : s);
        notifyObservers(ViewModelChangeType.SPRINTS);
    }

    @Override
    public void delete(SprintDTO item) {
        sprints.removeIf(s -> s.id().equals(item.id()));
        notifyObservers(ViewModelChangeType.SPRINTS);
    }

    public void setAll(List<SprintDTO> sprints) {
        this.sprints = sprints != null ? new ArrayList<>(sprints) : new ArrayList<>();
        notifyObservers(ViewModelChangeType.SPRINTS);
    }

    @Override
    protected void loadFrom(ObservableViewModel<?> loaded) {
        if (loaded instanceof SprintViewModel other) {
            this.sprints = other.sprints;
        }
    }
}
