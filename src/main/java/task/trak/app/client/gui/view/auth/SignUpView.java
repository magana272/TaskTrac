package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;

import javax.swing.*;
import java.awt.*;

public class SignUpView extends FormDialogView {
    private final AuthController authController;
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JPasswordField passwordField;

    public SignUpView(Component parent, AuthController authController) {
        super(parent, "Sign Up");
        this.authController = authController;
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 4, 4));
        usernameField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        return panel;
    }

    @Override
    protected void onConfirm() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (!username.isEmpty() && !password.isEmpty()) {
            authController.signup(username, firstName, lastName, email, password);
        }
    }
}
