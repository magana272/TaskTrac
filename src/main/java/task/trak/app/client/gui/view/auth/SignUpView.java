package task.trak.app.client.gui.view.auth;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JButton nextBtnRef;
    private JButton signUpBtnRef;
    private JDialog dialogRef;

    public SignUpView(Component parent, AuthController authController) {
        super(parent, "Sign Up");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        return new FormPanel(); // Not used — show() builds the multi-page layout
    }

    @Override
    public void show() {
        usernameField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        // ── Page 1: Account ──
        FormPanel page1 = new FormPanel();

        page1.addField("Email:", emailField);
        JPanel emailReq = makeReqPanel();
        emailFormatCheck = makeCheckLabel("Valid email");
        emailAvailCheck = makeCheckLabel("Available");
        emailReq.add(emailFormatCheck);
        emailReq.add(emailAvailCheck);
        page1.addField("", emailReq);

        page1.addField("Password:", passwordField);
        page1.addField("Confirm:", confirmPasswordField);
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
        page1.addField("", pwReq);

        page1.addField("Username:", usernameField);
        JPanel userReq = makeReqPanel();
        userLenCheck = makeCheckLabel("5–17 characters");
        userAlphaCheck = makeCheckLabel("Alphanumeric only");
        userAvailCheck = makeCheckLabel("Available");
        userReq.add(userLenCheck);
        userReq.add(userAlphaCheck);
        userReq.add(userAvailCheck);
        page1.addField("", userReq);

        JSeparator sep = new JSeparator();
        sep.setForeground(TrakTheme.BORDER);
        page1.addField("", sep);
        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/icons/google.png"));
        Image scaled = rawIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH);
        JButton googleBtn = new JButton("Sign up with Google", new ImageIcon(scaled));
        TrakTheme.styleButtonNav(googleBtn);
        googleBtn.setIconTextGap(8);
        googleBtn.setPreferredSize(new Dimension(220, 32));
        googleBtn.addActionListener(e -> onGoogleSignUp());
        page1.addField("", googleBtn);

        // ── Page 2: User Info ──
        FormPanel page2 = new FormPanel();
        page2.addField("First Name:", firstNameField);
        page2.addField("Last Name:", lastNameField);
        JPanel nameReq = makeReqPanel();
        firstNameCheck = makeCheckLabel("First name required");
        lastNameCheck = makeCheckLabel("Last name required");
        nameReq.add(firstNameCheck);
        nameReq.add(lastNameCheck);
        page2.addField("", nameReq);

        // ── Card layout ──
        CardLayout cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);
        pages.setOpaque(false);
        page1.setBackground(TrakTheme.BG_SURFACE);
        page2.setBackground(TrakTheme.BG_SURFACE);
        page1.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        page2.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        pages.add(page1, "page1");
        pages.add(page2, "page2");

        // ── Dialog ──
        Window owner = (parent instanceof Window) ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, title, true);
        this.dialogRef = dialog;
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);
        dialog.add(pages, BorderLayout.CENTER);

        // ── Buttons ──
        JButton cancelBtn = new JButton("Cancel");
        TrakTheme.styleButtonNav(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(80, 28));
        cancelBtn.addActionListener(e -> dialog.dispose());

        nextBtnRef = new JButton("Next");
        TrakTheme.styleButtonPrimary(nextBtnRef);
        nextBtnRef.setPreferredSize(new Dimension(80, 28));
        nextBtnRef.setEnabled(false);

        JButton backBtn = new JButton("Back");
        TrakTheme.styleButtonNav(backBtn);
        backBtn.setPreferredSize(new Dimension(80, 28));

        signUpBtnRef = new JButton("Sign Up");
        TrakTheme.styleButtonPrimary(signUpBtnRef);
        signUpBtnRef.setPreferredSize(new Dimension(100, 28));
        signUpBtnRef.setEnabled(false);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        buttonRow.setBackground(TrakTheme.BG_SURFACE);
        buttonRow.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_MD, TrakTheme.SP_LG));
        buttonRow.add(cancelBtn);
        buttonRow.add(nextBtnRef);

        nextBtnRef.addActionListener(e -> {
            cardLayout.show(pages, "page2");
            buttonRow.removeAll();
            buttonRow.add(backBtn);
            buttonRow.add(signUpBtnRef);
            buttonRow.revalidate();
            buttonRow.repaint();
            updateButtons();
        });

        backBtn.addActionListener(e -> {
            cardLayout.show(pages, "page1");
            buttonRow.removeAll();
            buttonRow.add(cancelBtn);
            buttonRow.add(nextBtnRef);
            buttonRow.revalidate();
            buttonRow.repaint();
        });

        signUpBtnRef.addActionListener(e -> { onConfirm(); dialog.dispose(); });

        dialog.add(buttonRow, BorderLayout.SOUTH);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(TrakTheme.BORDER, 1));

        // ── Validation ──
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

        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 250));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void onFieldChanged() {
        userAvailResult = false;
        emailAvailResult = false;
        updateCheck(userAvailCheck, false);
        updateCheck(emailAvailCheck, false);

        validateLocal();
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
                    updateButtons();
                });
            } catch (Exception ignored) {
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

        updateCheck(emailFormatCheck, email.contains("@") && email.contains("."));

        updateCheck(pwLenCheck, pw.length() >= 5 && pw.length() <= 16);
        updateCheck(pwLowerCheck, pw.matches(".*[a-z].*"));
        updateCheck(pwUpperCheck, pw.matches(".*[A-Z].*"));
        updateCheck(pwDigitCheck, pw.matches(".*\\d.*"));
        updateCheck(pwSymbolCheck, pw.matches(".*[^a-zA-Z0-9].*"));
        updateCheck(pwMatchCheck, !pw.isEmpty() && pw.equals(confirm));

        updateCheck(userLenCheck, user.length() >= 5 && user.length() <= 17);
        updateCheck(userAlphaCheck, !user.isEmpty() && user.matches("[a-zA-Z0-9]+"));

        updateCheck(firstNameCheck, !first.isEmpty());
        updateCheck(lastNameCheck, !last.isEmpty());

        updateButtons();
    }

    private void updateButtons() {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pw = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        boolean page1Valid =
                email.contains("@") && email.contains(".")
                && pw.length() >= 5 && pw.length() <= 16
                && pw.matches(".*[a-z].*") && pw.matches(".*[A-Z].*")
                && pw.matches(".*\\d.*") && pw.matches(".*[^a-zA-Z0-9].*")
                && pw.equals(confirm)
                && user.length() >= 5 && user.length() <= 17 && user.matches("[a-zA-Z0-9]+")
                && userAvailResult && emailAvailResult;

        nextBtnRef.setEnabled(page1Valid);

        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        signUpBtnRef.setEnabled(!first.isEmpty() && !last.isEmpty());
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

    private void onGoogleSignUp() {
        if (dialogRef != null) {
            dialogRef.dispose();
        }
        new Thread(() -> {
            try {
                String idToken = GoogleOAuthHelper.getIdToken();
                SwingUtilities.invokeLater(() -> authController.loginWithGoogle(idToken));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(parent, "Google sign-up failed: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
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
