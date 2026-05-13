package task.trak.app.client.gui.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clickable "+" placeholder shown when a view is empty.
 * Hover effect highlights the panel; click triggers a callback.
 */
public class AddPlaceholderPanel extends JPanel {

    private static final Color NORMAL_BG = new Color(0xF5, 0xF5, 0xF5);
    private static final Color HOVER_BG = new Color(0xE0, 0xE0, 0xE0);
    private boolean hovered = false;

    public AddPlaceholderPanel(String label, Runnable onClick) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(NORMAL_BG);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel plusIcon = new JLabel("+");
        plusIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        plusIcon.setForeground(new Color(0x90, 0x90, 0x90));
        plusIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(textLabel.getFont().deriveFont(Font.PLAIN, 14f));
        textLabel.setForeground(new Color(0x70, 0x70, 0x70));
        textLabel.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(plusIcon);
        add(Box.createRigidArea(new Dimension(0, 8)));
        add(textLabel);
        add(Box.createVerticalGlue());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                setBackground(HOVER_BG);
                plusIcon.setForeground(new Color(0x40, 0x40, 0x40));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                setBackground(NORMAL_BG);
                plusIcon.setForeground(new Color(0x90, 0x90, 0x90));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });

    }
}
