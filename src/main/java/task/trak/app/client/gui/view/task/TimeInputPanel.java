package task.trak.app.client.gui.view.task;

import task.trak.model.util.TimeUtil;
import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Structured duration input with days/hours/minutes spinners.
 */
public class TimeInputPanel extends JPanel {

    private final JSpinner daysSpinner;
    private final JSpinner hoursSpinner;
    private final JSpinner minutesSpinner;

    public TimeInputPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setOpaque(false);

        daysSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 24, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        add(daysSpinner);
        JLabel dLabel = new JLabel("days");
        dLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        add(dLabel);
        add(hoursSpinner);
        JLabel hLabel = new JLabel("hours");
        hLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        add(hLabel);
        add(minutesSpinner);
        JLabel mLabel = new JLabel("minutes");
        mLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        add(mLabel);
    }

    public String getDurationString() {
        int d = (int) daysSpinner.getValue();
        int h = (int) hoursSpinner.getValue();
        int m = (int) minutesSpinner.getValue();
        return d + "d " + h + "h " + m + "m";
    }

    public void setDuration(String duration) {
        int[] parts = TimeUtil.parseDurationToComponents(duration);
        daysSpinner.setValue(Math.min(parts[0], 30));
        hoursSpinner.setValue(Math.min(parts[1], 24));
        minutesSpinner.setValue(Math.min(parts[2], 59));
    }

    public boolean isZero() {
        return (int) daysSpinner.getValue() == 0
                && (int) hoursSpinner.getValue() == 0
                && (int) minutesSpinner.getValue() == 0;
    }
}
