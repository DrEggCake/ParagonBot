package com.dreggcake.src.Moderation;

import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.GuildContext;

public class autoUnban extends ListenerAdapter implements Runnable {

    @Override
    public void run() {
        try {
            // Fetch all bans
            List<BanRecord> bans = Databases.getMainDatabase().query(
                    "SELECT id, userid, reason, duration FROM bans",
                    rs -> {
                        List<BanRecord> list = new ArrayList<>();
                        while (rs.next()) {
                            list.add(new BanRecord(
                                    rs.getInt("id"),
                                    rs.getString("userid"),
                                    rs.getString("reason"),
                                    rs.getString("duration")
                            ));
                        }
                        return list;
                    }
            );

            long currentTime = System.currentTimeMillis();

            for (BanRecord ban : bans) {
                long banExpiry;

                try {
                    banExpiry = Long.parseLong(ban.duration);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid duration for user " + ban.userId + ", skipping.");
                    continue;
                }

                if (banExpiry <= currentTime) {
                    if (GuildContext.GUILD == null) continue;

                    // Attempt to retrieve user
                    GuildContext.JDA.retrieveUserById(ban.userId).queue(
                            user -> {
                                // Unban
                                GuildContext.GUILD.unban(user).queue(
                                        success -> sendUnbanLog(user, ban.reason),
                                        error -> System.out.println("Failed to unban user " + user.getAsTag())
                                );

                                deleteBanRecord(ban.id);
                            },
                            failure -> {
                                // User doesn't exist anymore
                                System.out.println("User " + ban.userId + " does not exist anymore. Removing ban record.");
                                deleteBanRecord(ban.id);
                            }
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteBanRecord(int id) {
        try {
            Databases.getMainDatabase().update(
                    "DELETE FROM bans WHERE id = ?",
                    id
            );
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

        if (Channels.PUNISHMENT_LOG != null) {
            Channels.PUNISHMENT_LOG
                    .sendMessageEmbeds(logEmbed.build())
                    .queue();
        }
    }

    // Helper class
    private static class BanRecord {
        int id;
        String userId;
        String reason;
        String duration;

        public BanRecord(int id, String userId, String reason, String duration) {
            this.id = id;
            this.userId = userId;
            this.reason = reason;
            this.duration = duration;
        }
    }
}