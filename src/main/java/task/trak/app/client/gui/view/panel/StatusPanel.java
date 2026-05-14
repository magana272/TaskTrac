package task.trak.app.client.gui.view.panel;

import task.trak.model.Session;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.auth.LoginView;
import task.trak.app.client.gui.view.auth.SignUpView;
import task.trak.app.client.gui.view.settings.ChangePasswordView;
import task.trak.app.client.gui.view.settings.DeleteAccountView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Premium editorial header bar.
 * Warm gold branding on deep dark surface. Generous breathing room.
 */
public class StatusPanel extends JPanel {

    private final JLabel userLabel;
    private final JLabel statusDot;
    private final JButton logoutButton;
    private final JButton settingsButton;
    private final JButton loginButton;
    private final JButton signupButton;
    private final JButton guestButton;
    private final GUIController controller;

    public StatusPanel(GUIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_XL, TrakTheme.SP_MD, TrakTheme.SP_XL));

        // ── Left: Brand + user ──
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("TRAK");
        titleLabel.setFont(TrakTheme.FONT_DISPLAY);
        titleLabel.setForeground(TrakTheme.ACCENT);

        // Thin separator line
        JLabel divider = new JLabel("  \u2502  ");
        divider.setForeground(TrakTheme.TEXT_MUTED);
        divider.setFont(TrakTheme.FONT_BODY);

        statusDot = new JLabel("\u25CF ");
        statusDot.setForeground(TrakTheme.TEXT_MUTED);
        statusDot.setFont(TrakTheme.FONT_CAPTION);

        userLabel = new JLabel("Not logged in");
        userLabel.setFont(TrakTheme.FONT_BODY);
        userLabel.setForeground(TrakTheme.TEXT_SECONDARY);

        leftPanel.add(titleLabel);
        leftPanel.add(divider);
        leftPanel.add(statusDot);
        leftPanel.add(userLabel);

        // ── Right: Auth buttons ──
        loginButton = new JButton("Login");
        TrakTheme.styleButtonAccent(loginButton);
        loginButton.addActionListener(e ->
                new LoginView(this, controller.getAuthController()).show());

        signupButton = new JButton("Sign Up");
        TrakTheme.styleButtonNav(signupButton);
        signupButton.addActionListener(e ->
                new SignUpView(this, controller.getAuthController()).show());

        guestButton = new JButton("Guest");
        TrakTheme.styleButtonNav(guestButton);
        guestButton.addActionListener(e ->
                controller.getAuthController().login("guest", "guest"));

        settingsButton = new JButton("\u2699 Settings");
        TrakTheme.styleButtonNav(settingsButton);
        settingsButton.addActionListener(e -> showSettingsMenu());

        logoutButton = new JButton("Logout");
        TrakTheme.styleButtonNav(logoutButton);
        logoutButton.addActionListener(e ->
                controller.getAuthController().logout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(loginButton);
        rightPanel.add(signupButton);
        rightPanel.add(guestButton);
        rightPanel.add(settingsButton);
        rightPanel.add(logoutButton);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    public void update(Session session) {
        if (session != null && session.getLogged_in_user() != null) {
            userLabel.setText(session.getLogged_in_user());
            userLabel.setForeground(TrakTheme.TEXT_PRIMARY);
            statusDot.setForeground(TrakTheme.ACCENT_GREEN);
            loginButton.setVisible(false);
            signupButton.setVisible(false);
            guestButton.setVisible(false);
            logoutButton.setVisible(true);
            settingsButton.setVisible(true);
        } else {
            userLabel.setText("Not logged in");
            userLabel.setForeground(TrakTheme.TEXT_SECONDARY);
            statusDot.setForeground(TrakTheme.TEXT_MUTED);
            loginButton.setVisible(true);
            signupButton.setVisible(true);
            guestButton.setVisible(true);
            logoutButton.setVisible(false);
            settingsButton.setVisible(false);
        }
    }

    private void showSettingsMenu() {
        String[] options = {"Change Password", "Delete Account", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Account Settings",
                "Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[2]);

        if (choice == 0) {
            new ChangePasswordView(this, controller.getAuthController()).show();
        } else if (choice == 1) {
            new DeleteAccountView(this, controller.getAuthController()).show();
        }
    }
}
