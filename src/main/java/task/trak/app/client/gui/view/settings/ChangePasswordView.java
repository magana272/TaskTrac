package task.trak.app.client.gui.view.settings;

import task.trak.app.client.gui.controller.AuthController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ChangePasswordView extends FormDialogView {

    private final AuthController authController;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private JLabel lengthCheck;
    private JLabel lowerCheck;
    private JLabel upperCheck;
    private JLabel digitCheck;
    private JLabel symbolCheck;
    private JLabel matchCheck;

    public ChangePasswordView(Component parent, AuthController authController) {
        super(parent, "Change Password");
        this.authController = authController;
    }

    @Override
    protected FormPanel buildPanel() {
        FormPanel form = new FormPanel();
        currentPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        form.addField("Current Password:", currentPasswordField);
        form.addField("New Password:", newPasswordField);
        form.addField("Confirm Password:", confirmPasswordField);

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
        newPasswordField.getDocument().addDocumentListener(validator);
        confirmPasswordField.getDocument().addDocumentListener(validator);
    }

    private void validate(JButton okBtn) {
        String pw = new String(newPasswordField.getPassword());
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
        String currentPass = new String(currentPasswordField.getPassword()).trim();
        String newPass = new String(newPasswordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();

        if (currentPass.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Current password is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(parent, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authController.changePassword(currentPass, newPass);
            JOptionPane.showMessageDialog(parent, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
