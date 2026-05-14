package task.trak.app.client.gui.viewmodel;

import task.trak.model.Session;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ObservableViewModel<Session> {

    private Session session;
    private String lastError;
    private String lastOutput;

    public UserViewModel() {
        super("user_viewmodel.ser");
    }

    @Override
    public List<Session> get() {
        List<Session> list = new ArrayList<>();
        if (session != null) list.add(session);
        return list;
    }

    @Override
    public void create(Session item) {
        this.session = item;
        notifyObservers(ViewModelChangeType.SESSION);
    }

    @Override
    public void update(Session item) {
        this.session = item;
        notifyObservers(ViewModelChangeType.SESSION);
    }

    @Override
    public void delete(Session item) {
        this.session = null;
        notifyObservers(ViewModelChangeType.SESSION);
    }

    public Session getSession() { return session; }

    public void setSession(Session session) {
        this.session = session;
        notifyObservers(ViewModelChangeType.SESSION);
    }

    public String getLastError() { return lastError; }

    public void setError(String error) {
        this.lastError = error;
        notifyObservers(ViewModelChangeType.ERROR);
    }

    public String getLastOutput() { return lastOutput; }

    public void setOutput(String output) {
        this.lastOutput = output;
        notifyObservers(ViewModelChangeType.OUTPUT);
    }

    @Override
    protected void loadFrom(ObservableViewModel<?> loaded) {
        if (loaded instanceof UserViewModel other) {
            this.session = other.session;
            this.lastError = other.lastError;
            this.lastOutput = other.lastOutput;
        }
    }
}
