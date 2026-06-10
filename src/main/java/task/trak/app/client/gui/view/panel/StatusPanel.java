package task.trak.app.client.gui.view.panel;

import task.trak.model.Session;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.auth.LoginView;
import task.trak.app.client.gui.view.auth.SignUpView;
import task.trak.app.client.gui.view.settings.ChangePasswordView;
import task.trak.app.client.gui.view.settings.ChangeUserInfoView;
import task.trak.app.client.gui.view.settings.DeleteAccountView;
import task.trak.app.client.config.WorkspaceConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Top status bar with user info and auth buttons.
 * Native macOS traffic lights handle close/minimize/fullscreen.
 */
public class StatusPanel extends JPanel {

    private final JLabel userLabel;
    private final JLabel statusDot;
    private final JButton logoutButton;
    private final JButton settingsButton;
    private final JButton themeToggle;
    private final JButton loginButton;
    private final JButton signupButton;
    private final JButton guestButton;
    private final GUIController controller;

    public StatusPanel(GUIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(new EmptyBorder(2, 72, TrakTheme.SP_SM, TrakTheme.SP_XL));

        // ── Left: Brand + user ──
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("TRAK");
        titleLabel.setFont(TrakTheme.FONT_DISPLAY);
        titleLabel.setForeground(TrakTheme.ACCENT);

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

        // ── Right: Auth buttons + theme toggle ──
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
                controller.getAuthController().login("guest", "Guest1!"));

        settingsButton = new JButton("\u2699 Settings");
        TrakTheme.styleButtonNav(settingsButton);
        settingsButton.addActionListener(e -> showSettingsMenu());

        logoutButton = new JButton("Logout");
        TrakTheme.styleButtonNav(logoutButton);
        logoutButton.addActionListener(e ->
                controller.getAuthController().logout());

        themeToggle = new JButton(TrakTheme.isDark() ? "\u2600" : "\u263D");
        themeToggle.setFont(TrakTheme.FONT_TITLE);
        themeToggle.setForeground(TrakTheme.TEXT_SECONDARY);
        themeToggle.setBackground(TrakTheme.BG_SURFACE);
        themeToggle.setOpaque(true);
        themeToggle.setBorderPainted(false);
        themeToggle.setFocusPainted(false);
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.setBorder(new EmptyBorder(2, 8, 2, 8));
        themeToggle.setToolTipText(TrakTheme.isDark() ? "Switch to Light Mode" : "Switch to Dark Mode");
        themeToggle.addActionListener(e -> toggleTheme());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(loginButton);
        rightPanel.add(signupButton);
        rightPanel.add(guestButton);
        rightPanel.add(settingsButton);
        rightPanel.add(logoutButton);
        rightPanel.add(themeToggle);

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

    private void toggleTheme() {
        TrakTheme.Theme next = TrakTheme.isDark() ? TrakTheme.Theme.LIGHT : TrakTheme.Theme.DARK;
        java.util.Map<Color, Color> colorMap = TrakTheme.setTheme(next);
        themeToggle.setText(TrakTheme.isDark() ? "\u2600" : "\u263D");
        themeToggle.setToolTipText(TrakTheme.isDark() ? "Switch to Light Mode" : "Switch to Dark Mode");
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) TrakTheme.recolorTree(w, colorMap);
        try {
            WorkspaceConfig config = WorkspaceConfig.load();
            config.setTheme(TrakTheme.isDark() ? "dark" : "light");
            config.save();
        } catch (Exception ignored) {}
    }

    private void showSettingsMenu() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Settings", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(TrakTheme.BG_SURFACE);
        content.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));

        // ── Profile Info ──
        JLabel profileLabel = new JLabel("Profile Info");
        profileLabel.setFont(TrakTheme.FONT_BODY);
        profileLabel.setForeground(TrakTheme.TEXT_MUTED);
        profileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(profileLabel);
        content.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        task.trak.model.dto.UserDTO userInfo = controller.getAuthController().getUserInfo();
        String username = userInfo != null && userInfo.userName() != null ? userInfo.userName() : "-";
        String firstName = userInfo != null && userInfo.firstName() != null ? userInfo.firstName() : "-";
        String lastName = userInfo != null && userInfo.lastName() != null ? userInfo.lastName() : "-";
        String email = userInfo != null && userInfo.email() != null ? userInfo.email() : "-";

        JLabel usernameLabel = new JLabel("Username:  " + username);
        usernameLabel.setFont(TrakTheme.FONT_BODY);
        usernameLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(usernameLabel);
        content.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        JLabel nameLabel = new JLabel("Name:  " + firstName + " " + lastName);
        nameLabel.setFont(TrakTheme.FONT_BODY);
        nameLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        JLabel emailLabel = new JLabel("Email:  " + email);
        emailLabel.setFont(TrakTheme.FONT_BODY);
        emailLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(emailLabel);
        content.add(Box.createVerticalStrut(TrakTheme.SP_SM));

        JButton changeInfoBtn = new JButton("Edit User Info");
        TrakTheme.styleButtonAccent(changeInfoBtn);
        changeInfoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        changeInfoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeInfoBtn.addActionListener(e -> {
            dialog.dispose();
            new ChangeUserInfoView(this, controller.getAuthController()).show();
        });
        content.add(changeInfoBtn);

        content.add(Box.createVerticalStrut(TrakTheme.SP_MD));

        // ── Account Settings ──
        JLabel accountLabel = new JLabel("Account Settings");
        accountLabel.setFont(TrakTheme.FONT_BODY);
        accountLabel.setForeground(TrakTheme.TEXT_MUTED);
        accountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(accountLabel);
        content.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        JButton changePassBtn = new JButton("Change Password");
        TrakTheme.styleButtonAccent(changePassBtn);
        changePassBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        changePassBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePassBtn.addActionListener(e -> {
            dialog.dispose();
            new ChangePasswordView(this, controller.getAuthController()).show();
        });
        content.add(changePassBtn);
        content.add(Box.createVerticalStrut(TrakTheme.SP_SM));

        JButton deleteAccBtn = new JButton("Delete Account");
        TrakTheme.styleButtonNav(deleteAccBtn);
        deleteAccBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        deleteAccBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteAccBtn.addActionListener(e -> {
            dialog.dispose();
            new DeleteAccountView(this, controller.getAuthController()).show();
        });
        content.add(deleteAccBtn);

        dialog.add(content, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        bottomRow.setBackground(TrakTheme.BG_SURFACE);
        bottomRow.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_MD, TrakTheme.SP_LG));

        JButton cancelBtn = new JButton("Cancel");
        TrakTheme.styleButtonNav(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(80, 28));
        cancelBtn.addActionListener(e -> dialog.dispose());
        bottomRow.add(cancelBtn);
        dialog.add(bottomRow, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(300, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
