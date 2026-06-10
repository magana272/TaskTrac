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

    public ChangeUserInfoView(Component parent, AuthController authController) {
        super(parent, "Edit User Info");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();

        task.trak.model.dto.UserDTO current = authController.getUserInfo();

        firstNameField = new JTextField(current != null ? current.firstName() : "");
        lastNameField = new JTextField(current != null ? current.lastName() : "");

        form.addField("First Name:", firstNameField);
        form.addField("Last Name:", lastNameField);
        return form;
    }

    @Override
    protected void onConfirm() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (firstName.isEmpty() && lastName.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Enter at least one field to update.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authController.changeUserInfo(
                    firstName.isEmpty() ? null : firstName,
                    lastName.isEmpty() ? null : lastName,
                    null);
            JOptionPane.showMessageDialog(parent, "User info updated.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
