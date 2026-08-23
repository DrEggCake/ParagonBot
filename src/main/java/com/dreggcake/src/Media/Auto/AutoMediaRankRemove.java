package com.dreggcake.src.Media.Auto;

import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.Roles;
import com.dreggcake.src.Utility.GuildContext;

public class AutoMediaRankRemove implements Runnable {

    @Override
    public void run() {
        try {
            long currentTime = System.currentTimeMillis();

            List<Long> expiredUsers = new ArrayList<>();

            Databases.getMediaDatabase().query(
                    "SELECT userid FROM media_ranks WHERE duration <= ?",
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            expiredUsers.add(rs.getLong("userid"));
                        }
                        return null;
                    },
                    currentTime
            );

            if (expiredUsers.isEmpty()) return;

            Guild guild = GuildContext.JDA.getGuilds().get(0); // safer than getFirst()

            for (long userId : expiredUsers) {
                Member member = guild.getMemberById(userId);

                if (member != null) {
                    // remove roles
                    guild.removeRoleFromMember(member, Roles.CONTENT_CREATOR_1).queue();
                    guild.removeRoleFromMember(member, Roles.CONTENT_CREATOR_2).queue();
                    guild.removeRoleFromMember(member, Roles.CONTENT_CREATOR_3).queue();
                    guild.removeRoleFromMember(member, Roles.CONTENT_CREATOR_4).queue();
                    guild.removeRoleFromMember(member, Roles.PARTNER).queue();

                    // ✅ log per user
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Auto Media Rank Expire");
                    embed.addField("Target", member.getAsMention(), true);
                    embed.setColor(Color.BLUE);

                    Channels.MEDIA_LOG
                            .sendMessageEmbeds(embed.build())
                            .queue();
                }

                // remove from DB
                Databases.getMediaDatabase().update(
                        "DELETE FROM media_ranks WHERE userid = ?",
                        userId
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}