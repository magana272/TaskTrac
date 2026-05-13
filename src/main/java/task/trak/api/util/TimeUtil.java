package task.trak.api.util;

import java.util.Date;

public class TimeUtil {

    public static String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        long totalSeconds = millis / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public static String formatDeadlineRemaining(Date deadline) {
        if (deadline == null) return "no deadline";
        long remaining = deadline.getTime() - System.currentTimeMillis();
        if (remaining <= 0) {
            return "overdue by " + formatDuration(-remaining);
        }
        return "in " + formatDuration(remaining);
    }
}
