package task.trak.app.server.model.backlog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BackLog {
    private final Long id;
    private final String project_name;
    private final Date created_at;
    private String name;
    private List<Long> task_ids;

    public BackLog(Long id, String name, String projectName, List<Long> taskIds, Date createdAt) {
        this.id = id;
        this.name = name;
        this.project_name = projectName;
        this.task_ids = taskIds != null ? taskIds : new ArrayList<>();
        this.created_at = createdAt != null ? createdAt : new Date();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject_name() {
        return this.project_name;
    }

    public List<Long> getTask_ids() {
        return this.task_ids;
    }

    public void setTask_ids(List<Long> task_ids) {
        this.task_ids = task_ids;
    }

    public Date getCreated_at() {
        return this.created_at;
    }

    public void addTask(Long taskId) {
        this.task_ids.add(taskId);
    }

    public void removeTask(Long taskId) {
        this.task_ids.remove(taskId);
    }

    public Integer getNumberOfTask() {
        return this.task_ids.size();
    }
}
