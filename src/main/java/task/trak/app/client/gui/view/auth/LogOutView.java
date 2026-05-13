package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;

import javax.swing.*;
import java.awt.*;

public class LogOutView extends FormDialogView {
    private final AuthController authController;

    public LogOutView(Component parent, AuthController authController) {
        super(parent, "Log Out");
        this.authController = authController;
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Are you sure you want to log out?"));
        return panel;
    }

    @Override
    protected void onConfirm() {
        authController.logout();
    }
}
