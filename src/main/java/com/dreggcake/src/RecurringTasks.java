package com.dreggcake.src;

import com.dreggcake.src.Database.DatabaseBackup;
import com.dreggcake.src.InviteTracker.InviteTracker;
import com.dreggcake.src.Media.Auto.AutoMediaRankRemove;
import com.dreggcake.src.Miscellaneous.PartnerReminder;
import com.dreggcake.src.Miscellaneous.ReminderClock;
import com.dreggcake.src.Miscellaneous.VoteReminder;
import com.dreggcake.src.Moderation.autoUnban;
import com.dreggcake.src.Moderation.autoUnmute;
import com.dreggcake.src.StaffList.StaffListRefresh;
import com.dreggcake.src.TicketSystem.AutoBot.AutoCloseService;
import com.dreggcake.src.TicketSystem.autoUnblacklistTicket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Registers all periodically running background tasks.
 */
public final class RecurringTasks {

    private RecurringTasks() {
    }

    public static void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleWithFixedDelay(new autoUnban(), 10, 10, TimeUnit.MINUTES);
        scheduler.scheduleWithFixedDelay(new autoUnmute(), 10, 20, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new DatabaseBackup("database.db"), 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new DatabaseBackup("counting.db"), 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new autoUnblacklistTicket(), 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(Main.INVITE_TRACKER::cacheInvites, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new Main.ActivityChanger(), 0L, 5L, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new StaffListRefresh(), 0, 24, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new AutoMediaRankRemove(), 0, 10, TimeUnit.MINUTES);

        // TicketSystem.AutoBot: periodic ticket activity scan (state persisted in DB)
        scheduler.scheduleWithFixedDelay(new AutoCloseService(), 1, 10, TimeUnit.MINUTES);

        // Miscellaneous: vote reminder once a day at noon, partner shoutout in the evening (server local time)
        long day = TimeUnit.DAYS.toMillis(1);
        long twoDay = TimeUnit.DAYS.toMillis(2);
        scheduler.scheduleAtFixedRate(new VoteReminder(),
                ReminderClock.millisUntil(VoteReminder.REMINDER_HOUR), day, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(new PartnerReminder(),
                ReminderClock.millisUntil(PartnerReminder.REMINDER_HOUR), twoDay, TimeUnit.MILLISECONDS);
    }
}
