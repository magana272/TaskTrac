package task.trak.app.client.gui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Dark-styled bottom command input bar with gold accent prompt.
 */
public class CommandInputPanel extends JPanel {

    private final JTextField inputField;

    public CommandInputPanel(Consumer<String> onSubmit) {
        setLayout(new BorderLayout(TrakTheme.SP_SM, 0));
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(TrakTheme.pad(TrakTheme.SP_SM, TrakTheme.SP_XL));

        JLabel prompt = new JLabel(" > ");
        prompt.setFont(TrakTheme.FONT_MONO.deriveFont(Font.BOLD));
        prompt.setForeground(TrakTheme.ACCENT);

        inputField = new JTextField();
        inputField.setFont(TrakTheme.FONT_MONO);
        inputField.setBackground(TrakTheme.BG_INPUT);
        inputField.setForeground(TrakTheme.TEXT_PRIMARY);
        inputField.setCaretColor(TrakTheme.ACCENT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(TrakTheme.BORDER, 1, true),
                new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_MD, TrakTheme.SP_SM, TrakTheme.SP_MD)
        ));

        JButton submitButton = new JButton("Run");
        TrakTheme.styleButtonAccent(submitButton);

        ActionListener action = e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                onSubmit.accept(text);
                inputField.setText("");
            }
        };
        inputField.addActionListener(action);
        submitButton.addActionListener(action);

        add(prompt, BorderLayout.WEST);
        add(inputField, BorderLayout.CENTER);
        add(submitButton, BorderLayout.EAST);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        inputField.setEnabled(enabled);
    }
}
