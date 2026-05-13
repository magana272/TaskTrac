package task.trak.app.server.model.project;

import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.user.User;

import java.util.Date;
import java.util.List;

public interface ProjectBuilder {
    TrakProjectBuilder setMembers(List<User> members);

    TrakProjectBuilder setProjectName(String name);

    TrakProjectBuilder setID(Long id);

    TrakProjectBuilder setCreationDate(Date date);

    TrakProjectBuilder setSprints(List<Sprint> sprints);

    TrakProjectBuilder setBack_log(BackLog back_log);

    TrakProjectBuilder setOwner(User user);

    TrakProjectBuilder setSummary(String summary);

    Project build();
}
