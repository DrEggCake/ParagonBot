package com.dreggcake.src;

import com.dreggcake.src.Counting.CountingDAO;
import com.dreggcake.src.Counting.CountingDatabaseBackup;
import com.dreggcake.src.Counting.CountingManager;
import com.dreggcake.src.Listeners.*;
import com.dreggcake.src.StaffList.StaffListRefresh;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class onReady extends ListenerAdapter {
    public static final Map<String, Integer> inviteCache = new HashMap<>();


    @Override
    public void onReady(ReadyEvent event) {
        Utilities.init(event.getJDA());
        Utilities.loadRuntimeValues();

        Guild guild = Utilities.GUILD;

        if (guild == null) {
            System.out.println("Guild not found!");
            return;
        }

        guild.retrieveInvites().queue(invites -> {
            inviteCache.clear();
            for (Invite invite : invites) {
                inviteCache.put(invite.getCode(), invite.getUses());
            }
            System.out.println("Invite cache loaded: " + inviteCache.size());
        });
        CountingDAO dao = new CountingDAO();

        dao.loadAll().forEach((channelId, state) -> {
            CountingManager.enable(channelId, state);
        });

        InviteTracker inviteTracker = new InviteTracker();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable autoUnban = new autoUnban();
        Runnable autoUnmute = new autoUnmute();
        Runnable databaseBackup = new databaseBackup();
        Runnable autoUnblacklistTicket = new autoUnblacklistTicket();
        Runnable inviteRefresh = new InviteCache(inviteTracker);
        Runnable countingDatabaseBackup = new CountingDatabaseBackup();
        Runnable staffListRefresh = new StaffListRefresh();

        scheduler.scheduleWithFixedDelay(autoUnban, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(autoUnmute, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(databaseBackup, 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(countingDatabaseBackup, 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(autoUnblacklistTicket, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(inviteRefresh, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new Main.ActivityChanger(), 0L, 5L, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(staffListRefresh, 0, 24, TimeUnit.HOURS);
    }
}



