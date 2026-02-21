package com.dreggcake.src.Listeners;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class autoUnban extends ListenerAdapter implements Runnable {

    @Override
    public void run() {
        String retrieve = "SELECT * FROM bans";

        try (Statement stmt = Main.conn.createStatement();
             ResultSet rs = stmt.executeQuery(retrieve)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String userId = rs.getString("userid");
                String reason = rs.getString("reason");
                long banExpiry;
                try {
                    banExpiry = Long.parseLong(rs.getString("duration"));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid duration for user " + userId + ", skipping.");
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                if (banExpiry <= currentTime) {
                    if (Utilities.GUILD == null) continue;

                    // Attempt to retrieve user
                    Main.jda.retrieveUserById(userId).queue(
                            user -> {
                                // User exists, unban normally
                                Utilities.GUILD.unban(user).queue(
                                        success -> sendUnbanLog(user, reason),
                                        error -> System.out.println("Failed to unban user " + user.getAsTag())
                                );
                                deleteBanRecord(id);
                            },
                            failure -> {
                                // User doesn't exist (deleted account)
                                System.out.println("User " + userId + " does not exist anymore. Removing ban record.");
                                deleteBanRecord(id);
                            }
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteBanRecord(int id) {
        String deleteSql = "DELETE FROM bans WHERE id = ?";
        try (PreparedStatement pstmt = Main.conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendUnbanLog(User user, String reason) {
        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setTitle("Automatic Unban");
        logEmbed.setDescription("User unbanned: " + user.getAsTag() + " (" + user.getId() + ")");
        logEmbed.addField("Reason for ban", reason, false);
        logEmbed.setColor(Color.BLUE);
        logEmbed.setThumbnail(user.getEffectiveAvatarUrl());

        if (Utilities.PUNISHMENT_LOG_CHANNEL_ID != null) {
            Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(logEmbed.build()).queue();
        }
    }
}
