package task.trak.app.client.gui.view.panel;

import task.trak.model.Session;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.auth.LoginView;
import task.trak.app.client.gui.view.auth.SignUpView;
import task.trak.app.client.gui.view.settings.ChangePasswordView;
import task.trak.app.client.gui.view.settings.DeleteAccountView;
import task.trak.app.client.config.WorkspaceConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Top status bar with branding, user info, and auth buttons.
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

        // Window controls (Spotify-style)
        JPanel windowControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        windowControls.setOpaque(false);

        JButton minimizeBtn = new JButton("\u2013");
        minimizeBtn.setFont(TrakTheme.FONT_BODY);
        minimizeBtn.setForeground(TrakTheme.STATUS_INPROGRESS);
        minimizeBtn.setBackground(TrakTheme.BG_SURFACE);
        minimizeBtn.setOpaque(true);
        minimizeBtn.setBorderPainted(false);
        minimizeBtn.setFocusPainted(false);
        minimizeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minimizeBtn.setBorder(new EmptyBorder(2, 8, 2, 8));
        minimizeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof Frame f) f.setExtendedState(Frame.ICONIFIED);
        });

        JButton closeBtn = new JButton("\u2715");
        closeBtn.setFont(TrakTheme.FONT_BODY);
        closeBtn.setForeground(TrakTheme.STATUS_READY);
        closeBtn.setBackground(TrakTheme.BG_SURFACE);
        closeBtn.setOpaque(true);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(2, 8, 2, 8));
        closeBtn.addActionListener(e -> System.exit(0));

        windowControls.add(themeToggle);
        windowControls.add(minimizeBtn);
        windowControls.add(closeBtn);

        // Right side: auth buttons + window controls
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(rightPanel, BorderLayout.CENTER);
        rightWrapper.add(windowControls, BorderLayout.EAST);

        add(leftPanel, BorderLayout.WEST);
        add(rightWrapper, BorderLayout.EAST);

        // Drag-to-move (anywhere on this panel)
        final Point[] dragStart = {null};
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { dragStart[0] = null; }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] == null) return;
                Window w = SwingUtilities.getWindowAncestor(StatusPanel.this);
                if (w != null) {
                    Point loc = w.getLocation();
                    w.setLocation(loc.x + e.getX() - dragStart[0].x, loc.y + e.getY() - dragStart[0].y);
                }
            }
        });
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

        JLabel titleLabel = new JLabel("Account Settings");
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(TrakTheme.BG_SURFACE);
        buttonPanel.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));

        JButton changePassBtn = new JButton("Change Password");
        TrakTheme.styleButtonAccent(changePassBtn);
        changePassBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        changePassBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePassBtn.addActionListener(e -> {
            dialog.dispose();
            new ChangePasswordView(this, controller.getAuthController()).show();
        });

        JButton deleteAccBtn = new JButton("Delete Account");
        TrakTheme.styleButtonNav(deleteAccBtn);
        deleteAccBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        deleteAccBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteAccBtn.addActionListener(e -> {
            dialog.dispose();
            new DeleteAccountView(this, controller.getAuthController()).show();
        });

        buttonPanel.add(changePassBtn);
        buttonPanel.add(Box.createVerticalStrut(TrakTheme.SP_SM));
        buttonPanel.add(deleteAccBtn);
        dialog.add(buttonPanel, BorderLayout.CENTER);

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
