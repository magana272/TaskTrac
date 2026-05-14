package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

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
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        usernameField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        form.addField("Username:", usernameField);
        form.addField("First Name:", firstNameField);
        form.addField("Last Name:", lastNameField);
        form.addField("Email:", emailField);
        form.addField("Password:", passwordField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Username is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (firstName.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "First name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (lastName.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Last name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Email is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(parent, "Email must contain '@'.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Password is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authController.signup(username, firstName, lastName, email, password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
