package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;

import javax.swing.*;
import java.awt.*;

public class LoginView extends FormDialogView {
    private final AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView(Component parent, AuthController authController) {
        super(parent, "Login");
        this.authController = authController;
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 4, 4));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        return panel;
    }

    @Override
    protected void onConfirm() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (!username.isEmpty() && !password.isEmpty()) {
            authController.login(username, password);
        }
    }
}
