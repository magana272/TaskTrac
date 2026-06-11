package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class ResetPasswordView extends FormDialogView {
    private final AuthController authController;
    private JTextField codeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ResetPasswordView(Component parent, AuthController authController) {
        super(parent, "Reset Password");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        codeField = new JTextField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        form.addField("Reset Code:", codeField);
        form.addField("New Password:", newPasswordField);
        form.addField("Confirm Password:", confirmPasswordField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String code = codeField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (code.isEmpty() || newPassword.isEmpty()) return;

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(parent, "Passwords do not match.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = authController.resetPassword(code, newPassword);
        if (result != null) {
            JOptionPane.showMessageDialog(parent, result, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
