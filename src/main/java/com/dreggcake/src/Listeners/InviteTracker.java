package com.dreggcake.src.Listeners;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.dreggcake.src.Main.conn;

public class InviteTracker extends ListenerAdapter {

    private final Map<String, Invite> inviteCache = new HashMap<>();
    private Guild guild;


    /* ================= DATABASE ================= */


    private void saveInvite(String inviterId, String invitedId, String code) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO invites (inviter_id, invited_id, invite_code, join_time) VALUES (?, ?, ?, ?)"
        )) {

            ps.setString(1, inviterId);
            ps.setString(2, invitedId);
            ps.setString(3, code);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ================= INVITE CACHE ================= */

    public void cacheInvites() {
        if (guild == null) return;
        guild.retrieveInvites().queue(invites -> {
            inviteCache.clear();
            for (Invite invite : invites) {
                inviteCache.put(invite.getCode(), invite);
            }
        });
    }

    /* ================= EVENTS ================= */

    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuilds().getFirst(); // single guild
        cacheInvites();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();

        guild.retrieveInvites().queue(invites -> {
            Invite used = null;

            for (Invite invite : invites) {
                Invite old = inviteCache.get(invite.getCode());
                if (old != null && invite.getUses() > old.getUses()) {
                    used = invite;
                    break;
                }
            }

            TextChannel log = guild.getTextChannelById(Utilities.INVITE_LOG_CHANNEL.getId());
            if (log == null) return;

            if (used != null && used.getInviter() != null) {
                User inviter = used.getInviter();

                saveInvite(inviter.getId(), member.getId(), used.getCode());

                log.sendMessage(
                        "📥 **" + member.getUser().getAsTag() +
                                "** was invited by **" + inviter.getAsTag() +
                                "** (`" + used.getCode() + "`)"
                ).queue();
            } else {
                log.sendMessage(
                        "📥 **" + member.getUser().getAsTag() + "** joined (invite unknown)"
                ).queue();
            }

            cacheInvites();
        });
    }

    @Override
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {
        cacheInvites();
        System.out.println("INVITE CREATED: " + event.getCode());
        TextChannel log = event.getGuild().getTextChannelById(Utilities.INVITE_LOG_CHANNEL.getId());
        if (log == null) return;

        Invite invite = event.getInvite();
        User inviter = invite.getInviter();

        log.sendMessage(
                "➕ **Invite created**\n" +
                        "Code: `" + invite.getCode() + "`\n" +
                        "By: **" + (inviter != null ? inviter.getAsTag() : "Unknown") + "**"
        ).queue();

    }

    @Override
    public void onGuildInviteDelete(GuildInviteDeleteEvent event) {
        cacheInvites();
        System.out.println("INVITE DELETED: " + event.getCode());

        TextChannel log = event.getGuild().getTextChannelById(Utilities.INVITE_LOG_CHANNEL.getId());
        if (log == null) return;

        log.sendMessage(
                "➖ **Invite deleted**\n" +
                        "Code: `" + event.getCode() + "`"
        ).queue();
    }

}
