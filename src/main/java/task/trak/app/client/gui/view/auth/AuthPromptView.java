package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Prompts the user to log in or sign up when an action requires authentication.
 */
public class AuthPromptView {

    private final Component parent;
    private final AuthController authController;
    private final String action;

    public AuthPromptView(Component parent, AuthController authController, String action) {
        this.parent = parent;
        this.authController = authController;
        this.action = action;
    }

    public void show() {
        Window owner = (parent instanceof Window) ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Authentication required", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel("Authentication required");
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG,
                TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("Please log in or sign up to " + action + ".");
        msgLabel.setFont(TrakTheme.FONT_BODY);
        msgLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        msgLabel.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG,
                TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(msgLabel, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        buttonRow.setBackground(TrakTheme.BG_SURFACE);
        buttonRow.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG,
                TrakTheme.SP_MD, TrakTheme.SP_LG));

        JButton cancelBtn = new JButton("Cancel");
        TrakTheme.styleButtonNav(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(80, 28));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton signUpBtn = new JButton("Sign Up");
        TrakTheme.styleButtonNav(signUpBtn);
        signUpBtn.setPreferredSize(new Dimension(80, 28));
        signUpBtn.addActionListener(e -> {
            dialog.dispose();
            new SignUpView(parent, authController).show();
        });

        JButton loginBtn = new JButton("Login");
        TrakTheme.styleButtonAccent(loginBtn);
        loginBtn.setPreferredSize(new Dimension(80, 28));
        loginBtn.addActionListener(e -> {
            dialog.dispose();
            new LoginView(parent, authController).show();
        });

        buttonRow.add(cancelBtn);
        buttonRow.add(signUpBtn);
        buttonRow.add(loginBtn);
        dialog.add(buttonRow, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}