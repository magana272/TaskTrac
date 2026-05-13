package task.trak.api.model;

public class Session {
    private String logged_in_user;
    private Long current_task_id;
    private Long task_started_at;

    public Session(String loggedInUser) {
        this.logged_in_user = loggedInUser;
    }

    public String getLogged_in_user() {
        return this.logged_in_user;
    }

    public void setLogged_in_user(String logged_in_user) {
        this.logged_in_user = logged_in_user;
    }

    public Long getCurrent_task_id() {
        return this.current_task_id;
    }

    public void setCurrent_task_id(Long current_task_id) {
        this.current_task_id = current_task_id;
    }

    public Long getTask_started_at() {
        return this.task_started_at;
    }

    public void setTask_started_at(Long task_started_at) {
        this.task_started_at = task_started_at;
    }
}
