package task.trak.app;

import task.trak.api.model.Session;

public interface App {
    void save();

    Session getSession();

    void setSession(Session session);
}
