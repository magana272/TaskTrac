package task.trak.app.client.gui.view.error;

public class EmailAlreadyExistErrorView extends ErrorAlertView {
    private final String email;

    public EmailAlreadyExistErrorView(String email) {
        super("Email Taken");
        this.email = email;
    }

    @Override
    protected String getMessage() {
        return "The email \"" + email + "\" is already in use. Please use a different email.";
    }
}
