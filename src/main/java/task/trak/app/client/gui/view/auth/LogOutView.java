package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class LogOutView extends FormDialogView {
    private final AuthController authController;

    public LogOutView(Component parent, AuthController authController) {
        super(parent, "Log Out");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        form.addField("", new JLabel("Are you sure you want to log out?"));
        return form;
    }

    @Override
    protected void onConfirm() {
        authController.logout();
    }
}
