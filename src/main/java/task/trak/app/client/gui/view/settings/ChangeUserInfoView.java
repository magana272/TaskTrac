package task.trak.app.client.gui.view.settings;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class ChangeUserInfoView extends FormDialogView {

    private final AuthController authController;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;

    public ChangeUserInfoView(Component parent, AuthController authController) {
        super(parent, "Change User Info");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        form.addField("First Name:", firstNameField);
        form.addField("Last Name:", lastNameField);
        form.addField("Email:", emailField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        if (firstName.isEmpty() && lastName.isEmpty() && email.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Enter at least one field to update.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authController.changeUserInfo(
                    firstName.isEmpty() ? null : firstName,
                    lastName.isEmpty() ? null : lastName,
                    email.isEmpty() ? null : email);
            JOptionPane.showMessageDialog(parent, "User info updated.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
