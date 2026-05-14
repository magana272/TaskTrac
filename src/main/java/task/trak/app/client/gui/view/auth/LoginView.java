package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

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
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        form.addField("Username:", usernameField);
        form.addField("Password:", passwordField);
        return form;
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
