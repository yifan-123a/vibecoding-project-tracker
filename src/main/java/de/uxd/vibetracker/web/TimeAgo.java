package de.uxd.vibetracker.web;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * "Updated 2 h ago" — the only human-facing time formatting in the app (M9).
 * Deliberately not a library dependency for four branches.
 */
public final class TimeAgo {

    private TimeAgo() {
    }

    public static String format(LocalDateTime moment) {
        if (moment == null) {
            return null;
        }
        Duration elapsed = Duration.between(moment, LocalDateTime.now());
        long minutes = elapsed.toMinutes();
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes == 1 ? "1 min ago" : minutes + " mins ago";
        }
        long hours = elapsed.toHours();
        if (hours < 24) {
            return hours + " h ago";
        }
        long days = elapsed.toDays();
        return days == 1 ? "1 day ago" : days + " days ago";
    }
}
