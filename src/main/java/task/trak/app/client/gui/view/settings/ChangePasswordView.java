package task.trak.app.client.gui.view.settings;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordView extends FormDialogView {

    private final AuthController authController;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePasswordView(Component parent, AuthController authController) {
        super(parent, "Change Password");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        currentPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        form.addField("Current Password:", currentPasswordField);
        form.addField("New Password:", newPasswordField);
        form.addField("Confirm Password:", confirmPasswordField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String currentPass = new String(currentPasswordField.getPassword()).trim();
        String newPass = new String(newPasswordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();

        if (currentPass.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Current password is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPass.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "New password is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(parent, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authController.changePassword(currentPass, newPass);
            JOptionPane.showMessageDialog(parent, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
