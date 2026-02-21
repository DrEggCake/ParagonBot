package com.dreggcake.src.Listeners;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class autoUnmute extends ListenerAdapter implements Runnable {

    @Override
    public void run() {
        String retrieve = "SELECT * FROM mutes";

        try (Statement stmt = Main.conn.createStatement();
             ResultSet rs = stmt.executeQuery(retrieve)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String userId = rs.getString("userid");
                String reason = rs.getString("reason");
                long muteExpiry;
                try {
                    muteExpiry = Long.parseLong(rs.getString("duration"));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid duration for user " + userId + ", skipping.");
                    continue;
                }

                if (muteExpiry <= System.currentTimeMillis()) {
                    if (Utilities.GUILD == null) continue;

                    Role muteRole = Utilities.MUTED_ROLE;
                    TextChannel logChannel = Utilities.PUNISHMENT_LOG_CHANNEL_ID;

                    Member member = Utilities.GUILD.getMemberById(userId);

                    // User exists in guild
                    if (member != null && muteRole != null) {
                        Utilities.GUILD.removeRoleFromMember(member, muteRole).queue();
                        sendUnmuteLog(member.getUser().getAsTag(), member.getId(), reason, logChannel, member.getEffectiveAvatarUrl());
                    } else {
                        // User left or account deleted
                        System.out.println("User " + userId + " not found in guild. Cleaning database entry.");
                        sendUnmuteLog("Unknown / Left", userId, reason, logChannel, null);
                    }

                    // Delete the mute record
                    deleteMuteRecord(id);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteMuteRecord(int id) {
        String deleteSql = "DELETE FROM mutes WHERE id = ?";
        try (PreparedStatement pstmt = Main.conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendUnmuteLog(String username, String userId, String reason, TextChannel logChannel, String avatarUrl) {
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
}
