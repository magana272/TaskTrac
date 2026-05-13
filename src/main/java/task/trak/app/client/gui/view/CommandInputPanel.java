package task.trak.app.client.gui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Bottom command input bar with a text field and Run button.
 */
public class CommandInputPanel extends JPanel {

    private final JTextField inputField;

    public CommandInputPanel(Consumer<String> onSubmit) {
        setLayout(new BorderLayout(4, 0));
        setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel prompt = new JLabel(" > ");
        prompt.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

        inputField = new JTextField();
        inputField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JButton submitButton = new JButton("Run");
        submitButton.setFocusPainted(false);

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
