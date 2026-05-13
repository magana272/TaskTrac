package task.trak.app.client.gui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ErrorPanel extends JPanel {

    private final JLabel messageLabel;
    private final Timer hideTimer;

    public ErrorPanel() {
        setLayout(new BorderLayout(8, 0));
        setBorder(new EmptyBorder(6, 12, 6, 12));
        setBackground(new Color(0xFD, 0xED, 0xED));
        setVisible(false);

        JLabel icon = new JLabel("\u26A0");
        icon.setFont(icon.getFont().deriveFont(16f));
        icon.setForeground(new Color(0xC6, 0x28, 0x28));

        messageLabel = new JLabel();
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 12f));
        messageLabel.setForeground(new Color(0xC6, 0x28, 0x28));

        JButton dismissBtn = new JButton("\u2715");
        dismissBtn.setFocusPainted(false);
        dismissBtn.setBorderPainted(false);
        dismissBtn.setContentAreaFilled(false);
        dismissBtn.setForeground(new Color(0xC6, 0x28, 0x28));
        dismissBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dismissBtn.addActionListener(e -> setVisible(false));

        add(icon, BorderLayout.WEST);
        add(messageLabel, BorderLayout.CENTER);
        add(dismissBtn, BorderLayout.EAST);

        hideTimer = new Timer(8000, e -> setVisible(false));
        hideTimer.setRepeats(false);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        setVisible(true);
        hideTimer.restart();
    }
}
