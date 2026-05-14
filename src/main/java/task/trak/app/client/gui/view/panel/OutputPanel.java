package task.trak.app.client.gui.view.panel;

import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;

/**
 * Dark terminal-style text area for displaying raw command output.
 */
public class OutputPanel extends JTextArea {

    public OutputPanel() {
        setEditable(false);
        setFont(TrakTheme.FONT_MONO);
        setBackground(TrakTheme.BG_DARK);
        setForeground(TrakTheme.TEXT_SECONDARY);
        setCaretColor(TrakTheme.ACCENT);
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
