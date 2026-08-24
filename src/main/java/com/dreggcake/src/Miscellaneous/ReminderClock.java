package com.dreggcake.src.Miscellaneous;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Scheduling math for fixed-time-of-day reminders (server local time).
 */
public final class ReminderClock {

    private ReminderClock() {
    }

    /**
     * Millis until the next occurrence of the given hour.
     */
    public static long millisUntil(int hour) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = now.toLocalDate().atStartOfDay(now.getZone()).plusHours(hour);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return Math.max(0, Duration.between(now, next).toMillis());
    }
}
