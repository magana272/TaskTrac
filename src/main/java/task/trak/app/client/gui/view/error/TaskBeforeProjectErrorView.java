package task.trak.app.client.gui.view.error;

public class TaskBeforeProjectErrorView extends ErrorAlertView {

    public TaskBeforeProjectErrorView() {
        super("No Project");
    }

    @Override
    protected String getMessage() {
        return "No projects found. Create a project first before adding tasks.";
    }
}
