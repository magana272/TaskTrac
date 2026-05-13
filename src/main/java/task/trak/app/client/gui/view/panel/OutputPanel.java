package task.trak.app.client.gui.view.panel;

import javax.swing.*;
import java.awt.*;

/**
 * Monospace text area for displaying raw command output.
 * Used as the fallback view inside ContentPanel.
 */
public class OutputPanel extends JTextArea {

    public OutputPanel() {
        setEditable(false);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        setBackground(new Color(0xFA, 0xFA, 0xFA));
    }

    public void appendCommand(String cmd) {
        append("> " + cmd + "\n");
        scrollToBottom();
    }

    public void appendOutput(String text) {
        if (text != null && !text.isEmpty()) {
            append(text);
            if (!text.endsWith("\n")) append("\n");
            scrollToBottom();
        }
    }

    public void appendError(String error) {
        append("ERROR: " + error + "\n");
        scrollToBottom();
    }

    private void scrollToBottom() {
        setCaretPosition(getDocument().getLength());
    }
}
