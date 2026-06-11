package task.trak.app.client.gui.view.error;

import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class ErrorAlertView {
    protected final String title;

    protected ErrorAlertView(String title) {
        this.title = title;
    }

    protected abstract String getMessage();

    public void show(Component parent) {
        Window owner = (parent instanceof Window) ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, title, true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.STATUS_READY);
        titleLabel.setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("<html><body style='width:260px'>" + getMessage() + "</body></html>");
        msgLabel.setFont(TrakTheme.FONT_BODY);
        msgLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        msgLabel.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(msgLabel, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        buttonRow.setBackground(TrakTheme.BG_SURFACE);
        buttonRow.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_MD, TrakTheme.SP_LG));

        JButton okBtn = new JButton("OK");
        TrakTheme.styleButtonPrimary(okBtn);
        okBtn.setPreferredSize(new Dimension(80, 28));
        okBtn.addActionListener(e -> dialog.dispose());
        buttonRow.add(okBtn);

        dialog.add(buttonRow, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(okBtn);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}