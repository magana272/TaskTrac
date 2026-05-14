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

    public static long parseDurationToMs(String duration) {
        int[] parts = parseDurationToComponents(duration);
        return ((long) parts[0] * 86400 + (long) parts[1] * 3600 + (long) parts[2] * 60) * 1000;
    }

    public static int[] parseDurationToComponents(String duration) {
        int days = 0, hours = 0, minutes = 0;
        if (duration == null || duration.isBlank()) {
            return new int[]{days, hours, minutes};
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+)\\s*([dhm])", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(duration);
        while (m.find()) {
            int val = Integer.parseInt(m.group(1));
            switch (m.group(2).toLowerCase()) {
                case "d" -> days = val;
                case "h" -> hours = val;
                case "m" -> minutes = val;
            }
        }
        return new int[]{days, hours, minutes};
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
