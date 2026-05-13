package task.trak.app.server.model.project;

import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.task.Task;
import task.trak.app.server.model.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Project {
    private final Long id;
    private final Date created_at;
    private final BackLog back_log;
    private final List<Sprint> sprints;
    private User owner;
    private List<User> members;
    private String project_name;
    private String summary;
    private Integer num_task;
    private Integer num_members;
    private Integer num_sprints;

    public Project(Long id, Date createdAt, BackLog backLog, List<Sprint> sprints, User owner, List<User> members, String projectName, String summary) {
        this.id = id;
        this.back_log = backLog;
        this.sprints = sprints;
        this.owner = owner;
        this.members = members;
        this.project_name = projectName;
        this.summary = summary;

        this.num_members = members == null ? 0 : members.size();
        this.num_sprints = sprints == null ? 0 : sprints.size();
        this.num_task = backLog == null ? 0 : backLog.getNumberOfTask();
        this.created_at = createdAt == null ? new Date(System.currentTimeMillis()) : createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public Date getCreated_at() {
        return this.created_at;
    }

    public BackLog getBack_log() {
        return this.back_log;
    }

    public List<Sprint> getSprints() {
        return this.sprints;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<User> getMembers() {
        return this.members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
        this.num_members = members == null ? 0 : members.size();
    }

    public String getName() {
        return this.project_name;
    }

    public void setName(String name) {
        this.project_name = name;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getNumberOfTasks() {
        return this.back_log != null ? this.back_log.getNumberOfTask() : 0;
    }

    public Integer getNumberOfMembers() {
        return this.members != null ? this.members.size() : 0;
    }

    public Integer getNumberOfSprints() {
        return this.sprints != null ? this.sprints.size() : 0;
    }

    public void add(User u) {
        this.members.add(u);
    }

    public void add(Sprint sprint) {
        this.sprints.add(sprint);
        this.num_sprints += 1;
    }

    public void add(Task t) {
        this.back_log.addTask(t.getId());
        this.num_task += 1;
    }

    public void remove(Task t) {
        this.back_log.removeTask(t.getId());
        this.num_task -= 1;
    }

    public void remove(User t) {
        this.num_members -= 1;
    }

    public void remove(Sprint s) {
        this.num_sprints -= 1;
    }

    public void removeMemberByFirstAndLast(String first_name, String last_name) {
        List<User> m = this.getMembers();
        m = m.stream().filter(user -> user.getName() == first_name + " " + last_name).toList();
        this.members = new ArrayList<User>(members);
        this.num_members = this.members.size();
    }

    public void removeMemberByID(Long id) {
        List<User> m = this.getMembers();
        m = m.stream().filter(user -> !user.getID().equals(id)).toList();
        new ArrayList<User>(members);
        this.num_members = this.members.size();
    }
}
