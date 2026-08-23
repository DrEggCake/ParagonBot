package com.dreggcake.src.InviteTracker;

import com.dreggcake.src.Utility.Channels;
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

/**
 * Listens for invite/join events, attributes joins to invites
 * and announces them in the invite log channel.
 */
public class InviteTracker extends ListenerAdapter {

    private final InviteCache cache = new InviteCache();
    private final InviteRepository repository = new InviteRepository();
    private Guild guild;

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
                if (cache.isKnown(invite.getCode()) && invite.getUses() > cache.uses(invite.getCode())) {
                    used = invite;
                    break;
                }
            }

            TextChannel log = Channels.INVITE_LOG;
            if (log == null) return;

            if (used != null && used.getInviter() != null) {
                User inviter = used.getInviter();

                repository.save(inviter.getId(), member.getId(), used.getCode());

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

        TextChannel log = Channels.INVITE_LOG;
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

        TextChannel log = Channels.INVITE_LOG;
        if (log == null) return;

        log.sendMessage(
                "➖ **Invite deleted**\n" +
                        "Code: `" + event.getCode() + "`"
        ).queue();
    }

    /* ================= CACHE ================= */

    public void cacheInvites() {
        cache.refresh(guild);
    }
}
