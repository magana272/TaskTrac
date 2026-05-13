package task.trak.app.client.gui.view.error;

public class ErrorView extends ErrorAlertView {
    private final String message;

    public ErrorView(String message) {
        super("Error");
        this.message = message;
    }

    @Override
    protected String getMessage() {
        return message;
    }
}
