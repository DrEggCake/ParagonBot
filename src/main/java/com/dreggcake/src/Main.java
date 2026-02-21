package com.dreggcake.src;

import com.dreggcake.src.AutoModService.AttachmentService.AttachmentListener;
import com.dreggcake.src.AutoModService.CapsService.CapsListener;
import com.dreggcake.src.AutoModService.Common.AutoDeleteListener;
import com.dreggcake.src.AutoModService.LinkService.LinkListener;
import com.dreggcake.src.AutoModService.SpamService.SpamListener;
import com.dreggcake.src.AutoModService.SwearService.SwearListener;
import com.dreggcake.src.Commands.Embeds.InfoEmbedCommand;
import com.dreggcake.src.Commands.Moderation.*;
import com.dreggcake.src.Commands.OtherModeration.UnbanForce;
import com.dreggcake.src.Commands.StaffList.StaffListCreateCommand;
import com.dreggcake.src.Commands.Ticket.TicketSetup;
import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Counting.CountingCommandsListener;
import com.dreggcake.src.Counting.CountingDatabaseBackup;
import com.dreggcake.src.Counting.CountingListener;
import com.dreggcake.src.Listeners.*;
import com.dreggcake.src.Listeners.Logs.TestLogging;
import com.dreggcake.src.Listeners.ReloadCommand.ConfigReload;
import com.dreggcake.src.StaffList.StaffRoleUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static JDA jda;
    public static Connection conn;


    public static void main(String[] args) throws IOException {
        Config.load();

        try {
            conn = DriverManager.getConnection(Config.getString("database.url", " "));
            System.out.println("[SQL] Connected to database");
        } catch (SQLException e) {
            System.err.println("[FATAL] Failed to connect to database");
            e.printStackTrace();
            return;
        }
        jda = JDABuilder.
                create(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_INVITES).setToken(Config.getString("bot.id", "")).build();
        jda.addEventListener(new onReady());
        jda.addEventListener(new Welcome());
        jda.addEventListener(new onSqlReady());
        jda.addEventListener(new TicketCreate());
        jda.addEventListener(new TicketCloseListen());
        jda.addEventListener(new Logging());
        jda.addEventListener(new ip());
        jda.addEventListener(new store());
        jda.addEventListener(new Set());
        jda.addEventListener(new InviteCommand());
        jda.addEventListener(new InviteTracker());
        jda.addEventListener(new CountingListener());
        jda.addEventListener(new CountingCommandsListener());
        jda.addEventListener(new AttachmentListener());
        jda.addEventListener(new CapsListener());
        jda.addEventListener(new LinkListener());
        jda.addEventListener(new SpamListener());
        jda.addEventListener(new SwearListener());
        jda.addEventListener(new AutoDeleteListener());
        jda.addEventListener(new StaffRoleUpdateListener());
        jda.addEventListener(new TestLogging());
        jda.addEventListener(new ConfigReload());

        CommandManager manager = new CommandManager();
        manager.add(new TicketSetup());
        manager.add(new BlackListTicket());
        manager.add(new UnBlackListTicket());
        manager.add(new Ban());
        manager.add(new Unban());
        manager.add(new ChangeBan());
        manager.add(new Mute());
        manager.add(new Unmute());
        manager.add(new InfoEmbedCommand());
        manager.add(new StaffListCreateCommand());
        manager.add(new UnbanForce());
        jda.addEventListener(manager);

        InviteTracker inviteTracker = new InviteTracker();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable autoUnban = new autoUnban();
        Runnable autoUnmute = new autoUnmute();
        Runnable databaseBackup = new databaseBackup();
        Runnable autoUnblacklistTicket = new autoUnblacklistTicket();
        Runnable inviteRefresh = new InviteCache(inviteTracker);
        Runnable countingDatabaseBackup = new CountingDatabaseBackup();

        scheduler.scheduleWithFixedDelay(autoUnban, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(autoUnmute, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(databaseBackup, 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(countingDatabaseBackup, 10, 3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(autoUnblacklistTicket, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(inviteRefresh, 10, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new ActivityChanger(), 0L, 5L, TimeUnit.SECONDS);

    }

    static class ActivityChanger implements Runnable {
        private final Activity[] activities = new Activity[]{Activity.playing("paragonnetwork.us"), Activity.listening("paragonnetwork.us"), Activity.watching("paragonnetwork.us")};
        private int currentIndex = 0;

        @Override
        public void run() {
            jda.getPresence().setActivity(this.activities[this.currentIndex]);
            this.currentIndex = (this.currentIndex + 1) % this.activities.length;
        }
    }
}



