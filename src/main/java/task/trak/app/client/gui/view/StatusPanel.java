package task.trak.app.client.gui.view;

import task.trak.api.model.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Top status bar showing login info on the left and a Logout button on the right.
 */
public class StatusPanel extends JPanel {

    private final JLabel userLabel;
    private final JButton logoutButton;
    private final JButton loginButton;
    private final JButton signupButton;
    private final JButton guestButton;
    private final Consumer<String> onCommand;
    private boolean guestMode = false;

    public StatusPanel(Consumer<String> onCommand) {
        this.onCommand = onCommand;
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
        loginButton.addActionListener(e -> showLoginDialog());

        signupButton = new JButton("Sign Up");
        signupButton.setFocusPainted(false);
        signupButton.addActionListener(e -> showSignupDialog());

        guestButton = new JButton("Continue as Guest");
        guestButton.setFocusPainted(false);
        guestButton.addActionListener(e -> {
            guestMode = true;
            onCommand.accept("login guest --password guest");
        });

        logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            guestMode = false;
            onCommand.accept("logout");
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(loginButton);
        rightPanel.add(signupButton);
        rightPanel.add(guestButton);
        rightPanel.add(logoutButton);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 4, 4));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (!username.isEmpty() && !password.isEmpty()) {
                onCommand.accept("login " + username + " --password " + password);
            }
        }
    }

    private void showSignupDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 4, 4));
        JTextField usernameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sign Up", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (!username.isEmpty() && !password.isEmpty()) {
                StringBuilder cmd = new StringBuilder("signup " + username);
                if (!firstName.isEmpty()) cmd.append(" --first_name ").append(firstName);
                if (!lastName.isEmpty()) cmd.append(" --last_name ").append(lastName);
                if (!email.isEmpty()) cmd.append(" --email ").append(email);
                cmd.append(" --password ").append(password);
                onCommand.accept(cmd.toString());
            }
        }
    }

    public void update(Session session) {
        if (session != null && session.getLogged_in_user() != null) {
            userLabel.setText("Logged in as: " + session.getLogged_in_user());
            loginButton.setVisible(false);
            signupButton.setVisible(false);
            guestButton.setVisible(false);
            logoutButton.setVisible(true);
            guestMode = false;
        } else if (guestMode) {
            userLabel.setText("Guest");
            loginButton.setVisible(true);
            signupButton.setVisible(true);
            guestButton.setVisible(false);
            logoutButton.setVisible(false);
        } else {
            userLabel.setText("Not logged in");
            loginButton.setVisible(true);
            signupButton.setVisible(true);
            guestButton.setVisible(true);
            logoutButton.setVisible(false);
        }
    }
}
