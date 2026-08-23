package com.dreggcake.src.TicketSystem;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class autoUnblacklistTicket extends ListenerAdapter implements Runnable {

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();

        try {
            // ✅ ONLY fetch expired blacklist entries
            List<BlacklistRecord> records = Databases.getMainDatabase().query(
                    "SELECT id, userid, reason, duration FROM blacklistTicket WHERE duration <= ?",
                    rs -> {
                        List<BlacklistRecord> list = new ArrayList<>();
                        while (rs.next()) {
                            list.add(new BlacklistRecord(
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

            for (BlacklistRecord record : records) {

                if (Utilities.GUILD == null) continue;

                Member member = Utilities.GUILD.getMemberById(record.userId);

                if (member != null) {
                    Utilities.GUILD.removeRoleFromMember(member, Utilities.TICKET_BLACKLIST_ROLE)
                            .queue(success -> {

                                EmbedBuilder embed = new EmbedBuilder();
                                embed.setTitle("Auto Ticket Blacklist Removed");
                                embed.addField("User:", member.getAsMention(), false);
                                embed.addField("ID:", record.userId, false);
                                embed.addField("Reason of blacklisting:", record.reason, false);
                                embed.setColor(Color.BLUE);
                                embed.setThumbnail(member.getEffectiveAvatarUrl());

                                Utilities.PUNISHMENT_LOG_CHANNEL_ID
                                        .sendMessageEmbeds(embed.build())
                                        .queue();
                            });

                } else {
                    System.out.println("User " + record.userId + " not found, removing record.");
                }

                // ✅ Always delete expired record (even if user not found)
                deleteRecord(record.id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRecord(int id) {
        try {
            Databases.getMainDatabase().update(
                    "DELETE FROM blacklistTicket WHERE id = ?",
                    id
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper class
    private static class BlacklistRecord {
        int id;
        String userId;
        String reason;
        String duration;

        public BlacklistRecord(int id, String userId, String reason, String duration) {
            this.id = id;
            this.userId = userId;
            this.reason = reason;
            this.duration = duration;
        }
    }
}