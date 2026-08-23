package com.dreggcake.src.Moderation;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class autoUnmute extends ListenerAdapter implements Runnable {

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();

        try {
            // ✅ ONLY fetch expired mutes
            List<MuteRecord> records = Databases.getMainDatabase().query(
                    "SELECT id, userid, reason, duration FROM mutes WHERE duration <= ?",
                    rs -> {
                        List<MuteRecord> list = new ArrayList<>();
                        while (rs.next()) {
                            list.add(new MuteRecord(
                                    rs.getInt("id"),
                                    rs.getString("userid"),
                                    rs.getString("reason"),
                                    rs.getString("duration")
                            ));
                        }
                        return list;
                    },
                    currentTime
            );

            for (MuteRecord record : records) {

                if (Utilities.GUILD == null) continue;

                Role muteRole = Utilities.MUTED_ROLE;
                TextChannel logChannel = Utilities.PUNISHMENT_LOG_CHANNEL_ID;

                Member member = Utilities.GUILD.getMemberById(record.userId);

                if (member != null && muteRole != null) {
                    Utilities.GUILD.removeRoleFromMember(member, muteRole).queue();

                    sendUnmuteLog(
                            member.getUser().getAsTag(),
                            member.getId(),
                            record.reason,
                            logChannel,
                            member.getEffectiveAvatarUrl()
                    );

                } else {
                    // User left / not found
                    System.out.println("User " + record.userId + " not found in guild. Cleaning database entry.");

                    sendUnmuteLog(
                            "Unknown / Left",
                            record.userId,
                            record.reason,
                            logChannel,
                            null
                    );
                }

                // ✅ Always delete expired mute
                deleteMuteRecord(record.id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteMuteRecord(int id) {
        try {
            Databases.getMainDatabase().update(
                    "DELETE FROM mutes WHERE id = ?",
                    id
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendUnmuteLog(String username, String userId, String reason,
                               TextChannel logChannel, String avatarUrl) {

        if (logChannel == null) return;

        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setTitle("Automatic Unmute");
        logEmbed.setDescription("User unmuted: " + username + " (" + userId + ")");
        logEmbed.addField("Reason for mute", reason, false);
        logEmbed.setColor(Color.BLUE);

        if (avatarUrl != null) {
            logEmbed.setThumbnail(avatarUrl);
        }

        logChannel.sendMessageEmbeds(logEmbed.build()).queue();
    }

    // Helper class
    private static class MuteRecord {
        int id;
        String userId;
        String reason;
        String duration;

        public MuteRecord(int id, String userId, String reason, String duration) {
            this.id = id;
            this.userId = userId;
            this.reason = reason;
            this.duration = duration;
        }
    }
}