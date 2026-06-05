package task.trak.app.client.gui.view.settings;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class DeleteAccountView extends FormDialogView {

    private final AuthController authController;
    private JPasswordField passwordField;

    public DeleteAccountView(Component parent, AuthController authController) {
        super(parent, "Delete Account");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        form.addField("", new JLabel("This will permanently delete your account and all data."));
        form.addField("", new JLabel("This action cannot be undone."));
        passwordField = new JPasswordField();
        form.addField("Confirm Password:", passwordField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String password = new String(passwordField.getPassword()).trim();

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Password is required to delete account.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to delete your account?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            authController.deleteAccount(password);
            JOptionPane.showMessageDialog(parent, "Account deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
