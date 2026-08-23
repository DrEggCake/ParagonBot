package com.dreggcake.src;

import com.dreggcake.src.Database.DatabaseBackup;
import com.dreggcake.src.InviteTracker.InviteTracker;
import com.dreggcake.src.Media.Auto.AutoMediaRankRemove;
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

        // TicketSystem.AutoBot: periodic ticket activity scan
        scheduler.scheduleWithFixedDelay(AutoCloseService.get(), 1, 10, TimeUnit.MINUTES);

        // Miscellaneous: recurring vote reminder
        scheduler.scheduleAtFixedRate(new VoteReminder(),
                VoteReminder.INTERVAL, VoteReminder.INTERVAL, TimeUnit.HOURS);
    }
}
