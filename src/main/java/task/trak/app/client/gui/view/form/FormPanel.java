package task.trak.app.client.gui.view.form;

import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;
import java.awt.*;

public class FormPanel extends JPanel {

    private final GridBagConstraints gbc = new GridBagConstraints();
    private int row = 0;

    public FormPanel() {
        super(new GridBagLayout());
        gbc.insets = new Insets(TrakTheme.SP_XS, TrakTheme.SP_XS, TrakTheme.SP_XS, TrakTheme.SP_XS);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
    }

    public void addField(String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(field, gbc);

        row++;
    }

    public void addExpandingField(String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(field, gbc);

        row++;
    }
}
