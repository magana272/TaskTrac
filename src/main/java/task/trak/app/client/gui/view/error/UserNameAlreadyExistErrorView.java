package task.trak.app.client.gui.view.error;

public class UserNameAlreadyExistErrorView extends ErrorAlertView {
    private final String username;

    public UserNameAlreadyExistErrorView(String username) {
        super("Username Taken");
        this.username = username;
    }

    @Override
    protected String getMessage() {
        return "The username \"" + username + "\" is already taken. Please choose a different username.";
    }
}
