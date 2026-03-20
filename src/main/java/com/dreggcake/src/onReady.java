package com.dreggcake.src;

import com.dreggcake.src.Counting.CountingDAO;
import com.dreggcake.src.Counting.CountingDatabaseBackup;
import com.dreggcake.src.Counting.CountingManager;
import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Listeners.*;
import com.dreggcake.src.Media.Auto.AutoMediaRankRemove;
import com.dreggcake.src.StaffList.StaffListRefresh;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class onReady extends ListenerAdapter {

    public static final Map<String, Integer> inviteCache = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {

        // ✅ STEP 1: INIT DATABASES + CREATE TABLES FIRST
        try {
            createMainDatabase();
            createCountingDatabase();
            createMediaDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            return; // stop if DB fails
        }

        // ✅ STEP 2: INIT UTILITIES
        Utilities.init(event.getJDA());
        Utilities.loadRuntimeValues();

        Guild guild = Utilities.GUILD;

        if (guild == null) {
            System.out.println("Guild not found!");
            return;
        }

        // ✅ STEP 3: INVITE CACHE
        guild.retrieveInvites().queue(invites -> {
            inviteCache.clear();
            for (Invite invite : invites) {
                inviteCache.put(invite.getCode(), invite.getUses());
            }
            System.out.println("Invite cache loaded: " + inviteCache.size());
        });

        // ✅ STEP 4: LOAD COUNTING (NOW SAFE)
        CountingDAO dao = new CountingDAO();
        dao.loadAll().forEach((channelId, state) -> {
            CountingManager.enable(channelId, state);
        });

        // ✅ STEP 5: SCHEDULERS
        InviteTracker inviteTracker = new InviteTracker();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleWithFixedDelay(new autoUnban(), 10, 10, TimeUnit.MINUTES);
        scheduler.scheduleWithFixedDelay(new autoUnmute(), 10, 20, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new databaseBackup(), 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new CountingDatabaseBackup(), 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new autoUnblacklistTicket(), 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new InviteCache(inviteTracker), 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new Main.ActivityChanger(), 0L, 5L, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new StaffListRefresh(), 0, 24, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new AutoMediaRankRemove(), 0, 10, TimeUnit.MINUTES);
    }

    // ================= DATABASE SETUP =================

    private void createMainDatabase() throws SQLException {
        Databases.getMainDatabase().init();

        Statement stmt = Databases.getMainDatabase().createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS mutes (id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)");
        stmt.execute("CREATE TABLE IF NOT EXISTS bans(id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)");
        stmt.execute("CREATE TABLE IF NOT EXISTS blacklistTicket(id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tickets (
                ticketid TEXT PRIMARY KEY,
                userid TEXT NOT NULL UNIQUE,
                created_at INTEGER
            )
        """);
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS invites (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                inviter_id TEXT,
                invited_id TEXT,
                invite_code TEXT,
                join_time INTEGER
            )
        """);
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS staff_list (
                guild_id TEXT NOT NULL,
                gamemode TEXT NOT NULL,
                channel_id TEXT NOT NULL,
                message_id TEXT NOT NULL,
                PRIMARY KEY (guild_id, gamemode)
            )
        """);

        stmt.close();
    }

    private void createCountingDatabase() throws SQLException {
        Databases.getCountingDatabase().init();

        Statement stmt = Databases.getCountingDatabase().createStatement();

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS counting_channels (
                channel_id INTEGER PRIMARY KEY,
                current_number INTEGER NOT NULL,
                last_user_id INTEGER NOT NULL,
                contributions TEXT NOT NULL
            )
        """);

        stmt.close();
    }

    private void createMediaDatabase() throws SQLException {
        Databases.getMediaDatabase().init();

        Statement stmt = Databases.getMediaDatabase().createStatement();

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS media_ranks(
                id INTEGER PRIMARY KEY,
                userid INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                staffid INTEGER NOT NULL
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS media_blacklists(
                id INTEGER PRIMARY KEY,
                userid INTEGER NOT NULL,
                reason TEXT NOT NULL,
                staffid INTEGER NOT NULL
            )
        """);

        stmt.close();
    }

}