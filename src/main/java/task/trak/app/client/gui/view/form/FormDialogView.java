package task.trak.app.client.gui.view.form;

import javax.swing.*;
import java.awt.*;

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
        JPanel panel = buildPanel();
        int result = JOptionPane.showConfirmDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            onConfirm();
        }
    }
}
