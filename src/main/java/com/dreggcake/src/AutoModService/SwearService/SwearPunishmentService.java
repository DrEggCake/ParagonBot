package com.dreggcake.src.AutoModService.SwearService;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SwearPunishmentService {

    private static final long WARNING_DURATION_MS = 10 * 60 * 1000L; // 10 minutes
    private static final long MUTE_DURATION_MS = 60 * 60 * 1000L; // 1 hour in ms

    // Track warnings: userId -> timestamp of last warning
    private static final Map<Long, Long> warningCache = new ConcurrentHashMap<>();

    public static void handleSwear(Message message, Member member) {
        Member bot = member.getGuild().getSelfMember();
        long userId = member.getIdLong();
        long now = System.currentTimeMillis();

        if (!canMute(bot, member))
            return;

        message.delete().queue(
                s -> {},
                f -> {}
        );

        Long lastWarning = warningCache.get(userId);

        //  First offense or expired warning → send warning
        if (lastWarning == null || now - lastWarning > WARNING_DURATION_MS) {
            warningCache.put(userId, now);
            sendWarning(message.getChannel().asTextChannel(), member);
            return;
        }

        //  Repeat offense within 10 minutes → mute user
        warningCache.remove(userId);
        addMutedRole(member);
        insertMuteIntoDatabase(Main.jda,member, MUTE_DURATION_MS);
        sendMuteEmbed(message.getChannel().asTextChannel(), member);
        sendMuteEmbed(Utilities.PUNISHMENT_LOG_CHANNEL_ID,member);
    }

    // ================= ACTIONS =================

    private static void addMutedRole(Member member) {
        Optional<Role> mutedRole = member.getGuild().getRoles().stream()
                .filter(r -> r.getId().equals(Utilities.MUTED_ROLE.getId()))
                .findFirst();

        mutedRole.ifPresent(role -> member.getGuild().addRoleToMember(member, role).queue());
    }

    private static void insertMuteIntoDatabase(JDA jda, Member member, long durationMs) {
        long unmuteTime = System.currentTimeMillis() + durationMs; // STORE IN MS

        String sql = "INSERT INTO mutes (userid,duration,reason,staffid) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = Main.conn.prepareStatement(sql);
            pstmt.setString(1, member.getId());
            pstmt.setLong(2, unmuteTime); // store as LONG not string
            pstmt.setString(3, "AutoMod: Repeated Swearing");
            pstmt.setString(4, jda.getSelfUser().getId());
            pstmt.execute();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error logging auto-mute: " + e.getMessage());
        }
    }

    // ================= EMBEDS =================

    private static void sendWarning(TextChannel channel, Member member) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setAuthor(member.getUser().getName()+" has been warned.", null, member.getEffectiveAvatarUrl())
                .addField("Reason:","Usage of prohibited language",true)
                .setDescription("This warning will expire in __**10 minutes**__")
                .setFooter("ParagonMC Auto Moderation")
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private static void sendMuteEmbed(TextChannel channel, Member member) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(member.getUser().getName()+" has been Muted", null, member.getEffectiveAvatarUrl())
                .addField("Reason", "Repeated swearing", false)
                .addField("Duration", "1 hour", false)
                .addField("Appeal","Think this was unfair? Appeal at "+Utilities.SUPPORT_CHANNEL.getAsMention(),false)
                .setFooter("ParagonMC Auto Moderation")
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    // ================= PERMISSIONS =================

    private static boolean canMute(Member bot, Member target) {
        if (!bot.hasPermission(Permission.MANAGE_ROLES))
            return false;

        if (!bot.canInteract(target))
            return false;

        if (target.isOwner())
            return false;

        return true;
    }
}
