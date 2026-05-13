package task.trak.app.client.gui.viewmodel;

import task.trak.api.dto.TaskDTO;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends ObservableViewModel<TaskDTO> {

    private List<TaskDTO> tasks = new ArrayList<>();
    private boolean showCompleted = false;
    private String sort = "None";
    private String projectFilter = "All";

    public TaskViewModel() {
        super("task_viewmodel.ser");
    }

    @Override
    public List<TaskDTO> get() {
        return tasks;
    }

    @Override
    public void create(TaskDTO item) {
        tasks.add(item);
        notifyObservers(ViewModelChangeType.TASKS);
    }

    @Override
    public void update(TaskDTO item) {
        tasks.replaceAll(t -> t.id().equals(item.id()) ? item : t);
        notifyObservers(ViewModelChangeType.TASKS);
    }

    @Override
    public void delete(TaskDTO item) {
        tasks.removeIf(t -> t.id().equals(item.id()));
        notifyObservers(ViewModelChangeType.TASKS);
    }

    public void setAll(List<TaskDTO> tasks) {
        this.tasks = tasks != null ? new ArrayList<>(tasks) : new ArrayList<>();
        notifyObservers(ViewModelChangeType.TASKS);
    }

    public List<TaskDTO> getFiltered() {
        List<TaskDTO> result = new ArrayList<>(tasks);

        if (!"All".equals(projectFilter)) {
            result.removeIf(t -> !projectFilter.equals(t.projectName()));
        }
        if (!showCompleted) {
            result.removeIf(t -> "COMPLETE".equals(t.status()));
        }
        if ("Due Date".equals(sort)) {
            result.sort((a, b) -> {
                if (a.deadline() == null && b.deadline() == null) return 0;
                if (a.deadline() == null) return 1;
                if (b.deadline() == null) return -1;
                return a.deadline().compareTo(b.deadline());
            });
        } else if ("Estimate".equals(sort)) {
            result.sort((a, b) -> Long.compare(parseEstimate(a.estimate()), parseEstimate(b.estimate())));
        }
        return result;
    }

    public boolean isShowCompleted() { return showCompleted; }
    public void setShowCompleted(boolean show) {
        this.showCompleted = show;
        notifyObservers(ViewModelChangeType.TASKS);
    }

    public String getSort() { return sort; }
    public void setSort(String sort) {
        this.sort = sort;
        notifyObservers(ViewModelChangeType.TASKS);
    }

    public String getProjectFilter() { return projectFilter; }
    public void setProjectFilter(String filter) {
        this.projectFilter = filter;
        notifyObservers(ViewModelChangeType.TASKS);
    }

    private long parseEstimate(String est) {
        if (est == null || est.isEmpty()) return Long.MAX_VALUE;
        try {
            String lower = est.trim().toLowerCase();
            if (lower.endsWith("h")) return (long) (Double.parseDouble(lower.replace("h", "")) * 60);
            if (lower.endsWith("d")) return (long) (Double.parseDouble(lower.replace("d", "")) * 60 * 8);
            if (lower.endsWith("m")) return (long) Double.parseDouble(lower.replace("m", ""));
            return Long.parseLong(lower);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    protected void loadFrom(ObservableViewModel<?> loaded) {
        if (loaded instanceof TaskViewModel other) {
            this.tasks = other.tasks;
            this.showCompleted = other.showCompleted;
            this.sort = other.sort;
            this.projectFilter = other.projectFilter;
        }
    }
}
