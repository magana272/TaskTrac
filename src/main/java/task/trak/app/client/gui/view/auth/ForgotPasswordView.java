package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordView extends FormDialogView {
    private final AuthController authController;
    private JTextField emailField;

    public ForgotPasswordView(Component parent, AuthController authController) {
        super(parent, "Forgot Password");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        emailField = new JTextField();
        form.addField("Email:", emailField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) return;

        String result = authController.requestPasswordReset(email);
        if (result != null) {
            JOptionPane.showMessageDialog(parent, result, "Password Reset", JOptionPane.INFORMATION_MESSAGE);
            new ResetPasswordView(parent, authController).show();
        }
    }
}
