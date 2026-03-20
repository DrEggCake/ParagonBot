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
import com.dreggcake.src.Counting.CountingListener;
import com.dreggcake.src.Database.DAO;
import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Listeners.*;
import com.dreggcake.src.Listeners.Logs.TestLogging;
import com.dreggcake.src.Listeners.ReloadCommand.ConfigReload;
import com.dreggcake.src.Media.Commands.MediaBlacklistCommand;
import com.dreggcake.src.Media.Commands.MediaRankCommand;
import com.dreggcake.src.Media.Commands.MediaUnblacklistCommand;
import com.dreggcake.src.Media.Commands.MediaUploadCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static JDA jda;
    public static Connection conn;


    public static void main(String[] args) throws IOException {
        Config.load();

        jda = JDABuilder.
                create(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_INVITES).setToken(Config.getString("bot.id", "")).build();
        jda.addEventListener(new onReady());
        jda.addEventListener(new Welcome());
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
//        jda.addEventListener(new StaffRoleUpdateListener());
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

        manager.add(new MediaRankCommand());
        manager.add(new MediaUploadCommand());
        manager.add(new MediaBlacklistCommand());
        manager.add(new MediaUnblacklistCommand());

        jda.addEventListener(manager);
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



