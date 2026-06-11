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

        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setForeground(new Color(100, 149, 237));
        forgotBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotBtn.setFont(forgotBtn.getFont().deriveFont(11f));
        forgotBtn.addActionListener(e -> new ForgotPasswordView(parent, authController).show());
        form.addField("", forgotBtn);

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
