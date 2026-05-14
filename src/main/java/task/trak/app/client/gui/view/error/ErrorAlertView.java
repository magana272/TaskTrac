package task.trak.app.client.gui.view.error;

import javax.swing.*;
import java.awt.*;

public abstract class ErrorAlertView {
    protected final String title;

    protected ErrorAlertView(String title) {
        this.title = title;
    }

    protected abstract String getMessage();

    public void show(Component parent) {
        JOptionPane.showMessageDialog(parent, getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }
}
