package task.trak.app.server.model.project;

import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.user.User;

import java.util.Date;
import java.util.List;

public class TrakProjectBuilder implements ProjectBuilder {
    private Long id;
    private Date created_at;
    private BackLog back_log;
    private List<Sprint> sprints;
    private User owner;
    private List<User> members;
    private String project_name;
    private String summary;
    private Integer num_member;

    @Override
    public TrakProjectBuilder setMembers(List<User> members) {
        this.members = members;
        this.num_member = members.size();
        return this;
    }

    @Override
    public TrakProjectBuilder setProjectName(String name) {
        this.project_name = name;
        return this;
    }

    @Override
    public TrakProjectBuilder setID(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public TrakProjectBuilder setCreationDate(Date date) {
        this.created_at = date;
        return this;
    }

    @Override
    public TrakProjectBuilder setSprints(List<Sprint> sprints) {
        this.sprints = sprints;
        return this;

    }

    @Override
    public TrakProjectBuilder setBack_log(BackLog back_log) {
        this.back_log = back_log;
        return this;

    }

    @Override
    public TrakProjectBuilder setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public TrakProjectBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    @Override
    public Project build() {
        return new Project(
                id,
                created_at,
                back_log,
                sprints,
                owner,
                members,
                project_name,
                summary
        );
    }
}
