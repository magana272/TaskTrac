package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SignUpView extends FormDialogView {
    private final AuthController authController;
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JLabel lengthCheck;
    private JLabel lowerCheck;
    private JLabel upperCheck;
    private JLabel digitCheck;
    private JLabel symbolCheck;
    private JLabel matchCheck;

    public SignUpView(Component parent, AuthController authController) {
        super(parent, "Sign Up");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        usernameField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        form.addField("Username:", usernameField);
        form.addField("First Name:", firstNameField);
        form.addField("Last Name:", lastNameField);
        form.addField("Email:", emailField);
        form.addField("Password:", passwordField);
        form.addField("Confirm:", confirmPasswordField);

        // Password requirements checklist
        JPanel reqPanel = new JPanel();
        reqPanel.setLayout(new BoxLayout(reqPanel, BoxLayout.Y_AXIS));
        reqPanel.setOpaque(false);

        lengthCheck = makeCheckLabel("5–16 characters");
        lowerCheck = makeCheckLabel("Lowercase letter");
        upperCheck = makeCheckLabel("Uppercase letter");
        digitCheck = makeCheckLabel("Number");
        symbolCheck = makeCheckLabel("Symbol");

        reqPanel.add(lengthCheck);
        reqPanel.add(lowerCheck);
        reqPanel.add(upperCheck);
        reqPanel.add(digitCheck);
        reqPanel.add(symbolCheck);
        matchCheck = makeCheckLabel("Passwords match");
        reqPanel.add(matchCheck);

        form.addField("", reqPanel);

        return form;
    }

    @Override
    protected void onDialogReady(JDialog dialog, JButton okBtn) {
        okBtn.setEnabled(false);

        DocumentListener validator = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validate(okBtn); }
            public void removeUpdate(DocumentEvent e) { validate(okBtn); }
            public void changedUpdate(DocumentEvent e) { validate(okBtn); }
        };
        passwordField.getDocument().addDocumentListener(validator);
        confirmPasswordField.getDocument().addDocumentListener(validator);

        validate(okBtn);
    }

    private void validate(JButton okBtn) {
        String pw = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        boolean lenOk = pw.length() >= 5 && pw.length() <= 16;
        boolean lowOk = pw.matches(".*[a-z].*");
        boolean upOk = pw.matches(".*[A-Z].*");
        boolean digOk = pw.matches(".*\\d.*");
        boolean symOk = pw.matches(".*[^a-zA-Z0-9].*");
        boolean matchOk = !pw.isEmpty() && pw.equals(confirm);

        updateCheck(lengthCheck, lenOk);
        updateCheck(lowerCheck, lowOk);
        updateCheck(upperCheck, upOk);
        updateCheck(digitCheck, digOk);
        updateCheck(symbolCheck, symOk);
        updateCheck(matchCheck, matchOk);

        okBtn.setEnabled(lenOk && lowOk && upOk && digOk && symOk && matchOk);
    }

    private JLabel makeCheckLabel(String text) {
        JLabel label = new JLabel("\u2717 " + text);
        label.setFont(TrakTheme.FONT_CAPTION);
        label.setForeground(TrakTheme.TEXT_MUTED);
        return label;
    }

    private void updateCheck(JLabel label, boolean passed) {
        String text = label.getText().substring(2); // strip old prefix
        if (passed) {
            label.setText("\u2713 " + text);
            label.setForeground(TrakTheme.ACCENT_GREEN);
        } else {
            label.setText("\u2717 " + text);
            label.setForeground(TrakTheme.TEXT_MUTED);
        }
    }

    @Override
    protected void onConfirm() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Username is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (firstName.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "First name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (lastName.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Last name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Email is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(parent, "Email must contain '@'.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authController.signup(username, firstName, lastName, email, password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
