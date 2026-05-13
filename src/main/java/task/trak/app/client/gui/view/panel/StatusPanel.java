package task.trak.app.client.gui.view.panel;

import task.trak.api.model.Session;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.auth.LoginView;
import task.trak.app.client.gui.view.auth.SignUpView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Top status bar showing login info on the left and auth buttons on the right.
 */
public class StatusPanel extends JPanel {

    private final JLabel userLabel;
    private final JButton logoutButton;
    private final JButton loginButton;
    private final JButton signupButton;
    private final JButton guestButton;
    private final GUIController controller;

    public StatusPanel(GUIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel titleLabel = new JLabel("Trak");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

        userLabel = new JLabel("Not logged in");
        userLabel.setFont(userLabel.getFont().deriveFont(Font.PLAIN, 12f));
        userLabel.setBorder(new EmptyBorder(0, 12, 0, 12));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(userLabel);

        loginButton = new JButton("Login");
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e ->
                new LoginView(this, controller.getAuthController()).show());

        signupButton = new JButton("Sign Up");
        signupButton.setFocusPainted(false);
        signupButton.addActionListener(e ->
                new SignUpView(this, controller.getAuthController()).show());

        guestButton = new JButton("Continue as Guest");
        guestButton.setFocusPainted(false);
        guestButton.addActionListener(e ->
                controller.getAuthController().login("guest", "guest"));

        logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e ->
                controller.getAuthController().logout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(loginButton);
        rightPanel.add(signupButton);
        rightPanel.add(guestButton);
        rightPanel.add(logoutButton);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    public void update(Session session) {
        if (session != null && session.getLogged_in_user() != null) {
            userLabel.setText("Logged in as: " + session.getLogged_in_user());
            loginButton.setVisible(false);
            signupButton.setVisible(false);
            guestButton.setVisible(false);
            logoutButton.setVisible(true);
        } else {
            userLabel.setText("Not logged in");
            loginButton.setVisible(true);
            signupButton.setVisible(true);
            guestButton.setVisible(true);
            logoutButton.setVisible(false);
        }
    }
}
