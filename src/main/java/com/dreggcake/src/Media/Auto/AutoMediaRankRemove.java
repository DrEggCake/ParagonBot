package com.dreggcake.src.Media.Auto;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutoMediaRankRemove implements Runnable {

    @Override
    public void run() {
        try {
            long currentTime = System.currentTimeMillis();

            List<Long> expiredUsers = new ArrayList<>();

            // get users whose stored time (epoch ms) has passed
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

            Guild guild = Main.jda.getGuilds().getFirst();

            for (long userId : expiredUsers) {
                Member member = guild.getMemberById(userId);

                if (member != null) {
                    // remove all possible media roles
                    guild.removeRoleFromMember(member, Utilities.CONTENT_CREATOR_1_ROLE).queue();
                    guild.removeRoleFromMember(member, Utilities.CONTENT_CREATOR_2_ROLE).queue();
                    guild.removeRoleFromMember(member, Utilities.CONTENT_CREATOR_3_ROLE).queue();
                    guild.removeRoleFromMember(member, Utilities.CONTENT_CREATOR_4_ROLE).queue();
                    guild.removeRoleFromMember(member, Utilities.PARTNER_ROLE).queue();
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