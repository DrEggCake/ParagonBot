package com.dreggcake.src;

import com.dreggcake.src.AutoModService.AttachmentService.AttachmentListener;
import com.dreggcake.src.AutoModService.CapsService.CapsListener;
import com.dreggcake.src.AutoModService.Common.AutoDeleteListener;
import com.dreggcake.src.AutoModService.LinkService.LinkListener;
import com.dreggcake.src.AutoModService.SpamService.SpamListener;
import com.dreggcake.src.AutoModService.SwearService.SwearListener;
import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Config.ConfigReloadCommand.ConfigReload;
import com.dreggcake.src.Counting.CountingCommandsListener;
import com.dreggcake.src.Counting.CountingListener;
import com.dreggcake.src.Embeds.InfoEmbedCommand;
import com.dreggcake.src.InviteTracker.InviteCommand;
import com.dreggcake.src.InviteTracker.InviteTracker;
import com.dreggcake.src.Logging.Logging;
import com.dreggcake.src.Media.Commands.MediaBlacklistCommand;
import com.dreggcake.src.Media.Commands.MediaRankCommand;
import com.dreggcake.src.Media.Commands.MediaUnblacklistCommand;
import com.dreggcake.src.Media.Commands.MediaUploadCommand;
import com.dreggcake.src.Miscellaneous.Commands.GuideCommand;
import com.dreggcake.src.Miscellaneous.NSCommands.Ip;
import com.dreggcake.src.Miscellaneous.NSCommands.Set;
import com.dreggcake.src.Miscellaneous.NSCommands.Store;
import com.dreggcake.src.Miscellaneous.NSCommands.Welcome;
import com.dreggcake.src.Moderation.*;
import com.dreggcake.src.StaffList.Commands.StaffListCreateCommand;
import com.dreggcake.src.TicketSystem.Commands.BlackListTicket;
import com.dreggcake.src.TicketSystem.Commands.TicketSetupCommand;
import com.dreggcake.src.TicketSystem.Commands.UnBlackListTicket;
import com.dreggcake.src.TicketSystem.Listeners.TicketClaimListener;
import com.dreggcake.src.TicketSystem.Listeners.TicketCloseListener;
import com.dreggcake.src.TicketSystem.Listeners.TicketCreateListener;
import com.dreggcake.src.TicketSystem.Listeners.TicketTypeListener;
import com.dreggcake.src.Utility.Emoji;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.sql.Connection;

public class Main extends ListenerAdapter {

    public static JDA jda;
    public static Connection conn;


    public static void main(String[] args) throws IOException {
        Config.load();

        jda = JDABuilder.create(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_INVITES).setToken(Config.getString("bot.id", "")).build();
        jda.addEventListener(new onReady());
        jda.addEventListener(new Welcome());

        jda.addEventListener(new TicketCreateListener());
        jda.addEventListener(new TicketCloseListener());
        jda.addEventListener(new TicketClaimListener());
        jda.addEventListener(new TicketTypeListener());

        jda.addEventListener(new Logging());
        jda.addEventListener(new Ip());
        jda.addEventListener(new Store());
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
        jda.addEventListener(new ConfigReload());

        CommandManager manager = new CommandManager();
        manager.add(new TicketSetupCommand());
        manager.add(new BlackListTicket());
        manager.add(new UnBlackListTicket());
        manager.add(new Ban());
        manager.add(new Unban());
        manager.add(new ChangeBan());
        manager.add(new Mute());
        manager.add(new Unmute());
        manager.add(new UnbanForce());
        manager.add(new InfoEmbedCommand());
        manager.add(new StaffListCreateCommand());
        manager.add(new GuideCommand());

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

    @Override
    public void onReady(ReadyEvent event) {
        Emoji.load(jda);
    }
}



