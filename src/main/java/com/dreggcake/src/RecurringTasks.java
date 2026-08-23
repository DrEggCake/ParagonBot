package com.dreggcake.src;

import com.dreggcake.src.Counting.CountingDatabaseBackup;
import com.dreggcake.src.Database.DatabaseBackup;
import com.dreggcake.src.InviteTracker.InviteTracker;
import com.dreggcake.src.Media.Auto.AutoMediaRankRemove;
import com.dreggcake.src.Moderation.autoUnban;
import com.dreggcake.src.Moderation.autoUnmute;
import com.dreggcake.src.StaffList.StaffListRefresh;
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
        scheduler.scheduleWithFixedDelay(new DatabaseBackup(), 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new CountingDatabaseBackup(), 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new autoUnblacklistTicket(), 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(Main.INVITE_TRACKER::cacheInvites, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new Main.ActivityChanger(), 0L, 5L, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new StaffListRefresh(), 0, 24, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new AutoMediaRankRemove(), 0, 10, TimeUnit.MINUTES);
    }
}
