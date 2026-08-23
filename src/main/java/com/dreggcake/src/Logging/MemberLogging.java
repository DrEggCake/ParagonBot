package com.dreggcake.src.Logging;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.dreggcake.src.Utility.Channels;

/**
 * Logs member changes (roles, nickname, avatars, global name) to the member log channel.
 */
public class MemberLogging extends ListenerAdapter {

    /**
     * Member ID -> Roles
     */
    private static final Map<String, List<Role>> MEMBER_ROLES = new ConcurrentHashMap<>();

    /* ---------------- READY ---------------- */

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot ready in " + event.getJDA().getGuilds().size() + " guilds.");
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Guild guild = event.getGuild();

        for (Member member : guild.getMembers()) {
            MEMBER_ROLES.put(member.getId(), member.getRoles());
        }
    }

    /* ---------------- ROLES ---------------- */

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        logRoles(event.getMember(), event.getRoles(), "Roles Added", Color.YELLOW);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        List<Role> before = MEMBER_ROLES.getOrDefault(event.getMember().getId(), List.of());
        List<Role> removed = before.stream()
                .filter(r -> !event.getMember().getRoles().contains(r))
                .toList();

        logRoles(event.getMember(), removed, "Roles Removed", Color.RED);
        MEMBER_ROLES.put(event.getMember().getId(), event.getMember().getRoles());
    }

    /* ---------------- PROFILE UPDATES ---------------- */

    @Override
    public void onGuildMemberUpdateAvatar(GuildMemberUpdateAvatarEvent event) {
        logAvatar("Server Avatar Updated", event.getMember().getUser());
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        logAvatar("Global Avatar Updated", event.getUser());
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Global Name Updated")
                .addField("Old", event.getOldGlobalName() == null ? "None" : event.getOldGlobalName(), true)
                .addField("New", event.getNewGlobalName() == null ? "None" : event.getNewGlobalName(), true)
                .setFooter(event.getUser().getId(), event.getUser().getEffectiveAvatarUrl());

        Channels.MEMBER_LOG.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Nickname Updated")
                .addField("Old", event.getOldNickname() == null ? "None" : event.getOldNickname(), true)
                .addField("New", event.getNewNickname() == null ? "None" : event.getNewNickname(), true)
                .setFooter(event.getUser().getId(), event.getUser().getEffectiveAvatarUrl());

        Channels.MEMBER_LOG.sendMessageEmbeds(embed.build()).queue();
    }

    /* ---------------- HELPERS ---------------- */

    private void logRoles(Member member, List<Role> roles, String title, Color color) {
        if (Channels.MEMBER_LOG == null || roles.isEmpty()) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setDescription(member.getAsMention());

        roles.forEach(r -> embed.appendDescription("\n• " + r.getAsMention()));
        Channels.MEMBER_LOG.sendMessageEmbeds(embed.build()).queue();
    }

    private void logAvatar(String title, User user) {
        if (Channels.MEMBER_LOG == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setImage(user.getEffectiveAvatarUrl())
                .setFooter(user.getId(), user.getEffectiveAvatarUrl());

        Channels.MEMBER_LOG.sendMessageEmbeds(embed.build()).queue();
    }
}
