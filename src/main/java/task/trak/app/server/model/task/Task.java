package task.trak.app.server.model.task;

import task.trak.api.service.STATE;

import java.util.Date;

public class Task {
    private final Long id;
    private final String project_name;
    private final Date created_at;
    private String assigned_to;
    private String title;
    private STATE status;
    private Date completed_at;
    private String summary;
    private Date deadline;
    private String estimate;
    private Long time_started;
    private Long time_spent_ms;

    public Task(Long id, String projectName, String assignedTo, String title, STATE status, Date createdAt, Date completedAt, String summary) {
        this.id = id;
        this.project_name = projectName;
        this.assigned_to = assignedTo;
        this.title = title;
        this.status = status != null ? status : STATE.READY;
        this.created_at = createdAt != null ? createdAt : new Date();
        this.completed_at = completedAt;
        this.summary = summary;
    }

    public Long getId() {
        return this.id;
    }

    public String getProject_name() {
        return this.project_name;
    }

    public String getAssigned_to() {
        return this.assigned_to;
    }

    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public STATE getStatus() {
        return this.status;
    }

    public void setStatus(STATE status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return this.created_at;
    }

    public Date getCompleted_at() {
        return this.completed_at;
    }

    public void setCompleted_at(Date completed_at) {
        this.completed_at = completed_at;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getDeadline() {
        return this.deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getEstimate() {
        return this.estimate;
    }

    public void setEstimate(String estimate) {
        this.estimate = estimate;
    }

    public Long getTime_started() {
        return this.time_started;
    }

    public void setTime_started(Long time_started) {
        this.time_started = time_started;
    }

    public Long getTime_spent_ms() {
        return this.time_spent_ms;
    }

    public void setTime_spent_ms(Long time_spent_ms) {
        this.time_spent_ms = time_spent_ms;
    }

    public long getElapsedMs() {
        long accumulated = this.time_spent_ms != null ? this.time_spent_ms : 0;
        if (this.time_started != null) {
            accumulated += System.currentTimeMillis() - this.time_started;
        }
        return accumulated;
    }
}
