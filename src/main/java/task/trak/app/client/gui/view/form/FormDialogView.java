package task.trak.app.client.gui.view.form;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;

import task.trak.app.client.gui.view.TrakTheme;

public abstract class FormDialogView {
    protected final Component parent;
    protected final String title;

    protected FormDialogView(Component parent, String title) {
        this.parent = parent;
        this.title = title;
    }

    protected abstract FormPanel buildPanel();

    protected abstract void onConfirm();

    public void show() {
        FormPanel panel = buildPanel();

        Window owner = (parent instanceof Window) ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, title, true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setBorder(new javax.swing.border.EmptyBorder(
                TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        dialog.add(titleLabel, BorderLayout.NORTH);

        panel.setBorder(new javax.swing.border.EmptyBorder(
                TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        panel.setBackground(TrakTheme.BG_SURFACE);
        dialog.add(panel, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        buttonRow.setBackground(TrakTheme.BG_SURFACE);
        buttonRow.setBorder(new javax.swing.border.EmptyBorder(
                TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_MD, TrakTheme.SP_LG));

        JButton okBtn = new JButton("OK");
        TrakTheme.styleButtonPrimary(okBtn);
        okBtn.setPreferredSize(new Dimension(80, 28));
        okBtn.addActionListener(e -> { onConfirm(); dialog.dispose(); });

        JButton cancelBtn = new JButton("Cancel");
        TrakTheme.styleButtonNav(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(80, 28));
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonRow.add(cancelBtn);
        buttonRow.add(okBtn);
        dialog.add(buttonRow, BorderLayout.SOUTH);

        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(TrakTheme.BORDER, 1));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 250));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
