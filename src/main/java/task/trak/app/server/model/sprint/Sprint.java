package task.trak.app.server.model.sprint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Sprint {
    private final Long id;
    private final String project_name;
    private String name;
    private List<Long> task_ids;
    private Date start_date;
    private Date end_date;

    public Sprint(Long id, String projectName, String name, List<Long> taskIds, Date startDate, Date endDate) {
        this.id = id;
        this.project_name = projectName;
        this.name = name;
        this.task_ids = taskIds != null ? taskIds : new ArrayList<>();
        this.start_date = startDate;
        this.end_date = endDate;
    }

    public Long getId() {
        return this.id;
    }

    public String getProject_name() {
        return this.project_name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getTask_ids() {
        return this.task_ids;
    }

    public void setTask_ids(List<Long> task_ids) {
        this.task_ids = task_ids;
    }

    public Date getStart_date() {
        return this.start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return this.end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }
}
