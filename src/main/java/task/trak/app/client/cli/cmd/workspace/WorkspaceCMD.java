package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.model.Session;
import task.trak.app.App;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.cmdtype.CMD;

public abstract class WorkspaceCMD implements CMD<Object> {
    protected Session session;

    protected WorkspaceCMD() {
        App app = TTApp.getInstance();
        this.session = app != null ? app.getSession() : null;
        if (this.session == null || this.session.getLogged_in_user() == null) {
            throw new IllegalStateException("You must be logged in. Run: trak login <username> --password <password>");
        }
    }

    protected String getCurrentUsername() {
        return this.session.getLogged_in_user();
    }
}
