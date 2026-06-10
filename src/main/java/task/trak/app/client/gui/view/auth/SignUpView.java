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

    // Username checks
    private JLabel userLenCheck;
    private JLabel userAlphaCheck;
    private JLabel userAvailCheck;

    // Name checks
    private JLabel firstNameCheck;
    private JLabel lastNameCheck;

    // Email checks
    private JLabel emailFormatCheck;
    private JLabel emailAvailCheck;

    // Password checks
    private JLabel pwLenCheck;
    private JLabel pwLowerCheck;
    private JLabel pwUpperCheck;
    private JLabel pwDigitCheck;
    private JLabel pwSymbolCheck;
    private JLabel pwMatchCheck;

    // Async availability state
    private volatile boolean userAvailResult = false;
    private volatile boolean emailAvailResult = false;
    private Timer asyncTimer;
    private JButton okBtnRef;

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

        // Username
        form.addField("Username:", usernameField);
        JPanel userReq = makeReqPanel();
        userLenCheck = makeCheckLabel("5–17 characters");
        userAlphaCheck = makeCheckLabel("Alphanumeric only");
        userAvailCheck = makeCheckLabel("Available");
        userReq.add(userLenCheck);
        userReq.add(userAlphaCheck);
        userReq.add(userAvailCheck);
        form.addField("", userReq);

        // Name
        form.addField("First Name:", firstNameField);
        form.addField("Last Name:", lastNameField);
        JPanel nameReq = makeReqPanel();
        firstNameCheck = makeCheckLabel("First name required");
        lastNameCheck = makeCheckLabel("Last name required");
        nameReq.add(firstNameCheck);
        nameReq.add(lastNameCheck);
        form.addField("", nameReq);

        // Email
        form.addField("Email:", emailField);
        JPanel emailReq = makeReqPanel();
        emailFormatCheck = makeCheckLabel("Valid email");
        emailAvailCheck = makeCheckLabel("Available");
        emailReq.add(emailFormatCheck);
        emailReq.add(emailAvailCheck);
        form.addField("", emailReq);

        // Password
        form.addField("Password:", passwordField);
        form.addField("Confirm:", confirmPasswordField);
        JPanel pwReq = makeReqPanel();
        pwLenCheck = makeCheckLabel("5–16 characters");
        pwLowerCheck = makeCheckLabel("Lowercase letter");
        pwUpperCheck = makeCheckLabel("Uppercase letter");
        pwDigitCheck = makeCheckLabel("Number");
        pwSymbolCheck = makeCheckLabel("Symbol");
        pwMatchCheck = makeCheckLabel("Passwords match");
        pwReq.add(pwLenCheck);
        pwReq.add(pwLowerCheck);
        pwReq.add(pwUpperCheck);
        pwReq.add(pwDigitCheck);
        pwReq.add(pwSymbolCheck);
        pwReq.add(pwMatchCheck);
        form.addField("", pwReq);

        return form;
    }

    @Override
    protected void onDialogReady(JDialog dialog, JButton okBtn) {
        this.okBtnRef = okBtn;
        okBtn.setEnabled(false);

        // Debounce timer for async availability checks (400ms after last keystroke)
        asyncTimer = new Timer(400, e -> checkAvailabilityAsync());
        asyncTimer.setRepeats(false);

        DocumentListener validator = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onFieldChanged(); }
            public void removeUpdate(DocumentEvent e) { onFieldChanged(); }
            public void changedUpdate(DocumentEvent e) { onFieldChanged(); }
        };
        usernameField.getDocument().addDocumentListener(validator);
        firstNameField.getDocument().addDocumentListener(validator);
        lastNameField.getDocument().addDocumentListener(validator);
        emailField.getDocument().addDocumentListener(validator);
        passwordField.getDocument().addDocumentListener(validator);
        confirmPasswordField.getDocument().addDocumentListener(validator);

        validateLocal();
    }

    private void onFieldChanged() {
        // Reset async checks while typing
        userAvailResult = false;
        emailAvailResult = false;
        updateCheck(userAvailCheck, false);
        updateCheck(emailAvailCheck, false);

        validateLocal();

        // Restart debounce timer for network checks
        asyncTimer.restart();
    }

    private void checkAvailabilityAsync() {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        boolean userFormatOk = user.length() >= 5 && user.length() <= 17 && user.matches("[a-zA-Z0-9]+");
        boolean emailFormatOk = email.contains("@") && email.contains(".");

        new Thread(() -> {
            try {
                boolean userOk = userFormatOk && !authController.usernameExists(user);
                boolean emailOk = emailFormatOk && !authController.emailExists(email);

                SwingUtilities.invokeLater(() -> {
                    userAvailResult = userOk;
                    emailAvailResult = emailOk;
                    updateCheck(userAvailCheck, userFormatOk && userOk);
                    updateCheck(emailAvailCheck, emailFormatOk && emailOk);
                    updateOkButton();
                });
            } catch (Exception ignored) {
                // Network error — leave as unchecked, server validates on submit
            }
        }, "signup-avail-check").start();
    }

    private void validateLocal() {
        String user = usernameField.getText().trim();
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String pw = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        updateCheck(userLenCheck, user.length() >= 5 && user.length() <= 17);
        updateCheck(userAlphaCheck, !user.isEmpty() && user.matches("[a-zA-Z0-9]+"));

        updateCheck(firstNameCheck, !first.isEmpty());
        updateCheck(lastNameCheck, !last.isEmpty());

        updateCheck(emailFormatCheck, email.contains("@") && email.contains("."));

        updateCheck(pwLenCheck, pw.length() >= 5 && pw.length() <= 16);
        updateCheck(pwLowerCheck, pw.matches(".*[a-z].*"));
        updateCheck(pwUpperCheck, pw.matches(".*[A-Z].*"));
        updateCheck(pwDigitCheck, pw.matches(".*\\d.*"));
        updateCheck(pwSymbolCheck, pw.matches(".*[^a-zA-Z0-9].*"));
        updateCheck(pwMatchCheck, !pw.isEmpty() && pw.equals(confirm));

        updateOkButton();
    }

    private void updateOkButton() {
        String user = usernameField.getText().trim();
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String pw = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        boolean allLocal =
                user.length() >= 5 && user.length() <= 17 && user.matches("[a-zA-Z0-9]+")
                && !first.isEmpty() && !last.isEmpty()
                && email.contains("@") && email.contains(".")
                && pw.length() >= 5 && pw.length() <= 16
                && pw.matches(".*[a-z].*") && pw.matches(".*[A-Z].*")
                && pw.matches(".*\\d.*") && pw.matches(".*[^a-zA-Z0-9].*")
                && pw.equals(confirm);

        okBtnRef.setEnabled(allLocal && userAvailResult && emailAvailResult);
    }

    private JPanel makeReqPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    private JLabel makeCheckLabel(String text) {
        JLabel label = new JLabel("\u2717 " + text);
        label.setFont(TrakTheme.FONT_CAPTION);
        label.setForeground(TrakTheme.TEXT_MUTED);
        return label;
    }

    private void updateCheck(JLabel label, boolean passed) {
        String text = label.getText().substring(2);
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

        try {
            authController.signup(username, firstName, lastName, email, password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
