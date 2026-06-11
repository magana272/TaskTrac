package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import java.awt.*;

public class LoginView extends FormDialogView {
    private final AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JDialog dialogRef;

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

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(TrakTheme.BORDER);
        form.addField("", sep);

        // Google sign-in button
        JButton googleBtn = new JButton("Sign in with Google");
        TrakTheme.styleButtonNav(googleBtn);
        googleBtn.setPreferredSize(new Dimension(200, 32));
        googleBtn.addActionListener(e -> onGoogleSignIn());
        form.addField("", googleBtn);

        return form;
    }

    @Override
    protected void onDialogReady(JDialog dialog, JButton okBtn) {
        this.dialogRef = dialog;
    }

    @Override
    protected void onConfirm() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (!username.isEmpty() && !password.isEmpty()) {
            authController.login(username, password);
        }
    }

    private void onGoogleSignIn() {
        if (dialogRef != null) {
            dialogRef.dispose();
        }
        new Thread(() -> {
            try {
                String idToken = GoogleOAuthHelper.getIdToken();
                SwingUtilities.invokeLater(() -> authController.loginWithGoogle(idToken));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(parent, "Google sign-in failed: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }
}
