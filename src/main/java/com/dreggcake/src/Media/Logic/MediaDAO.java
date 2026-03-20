package com.dreggcake.src.Media.Logic;


import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaDAO {

    public static void addMemberRank(Member target, long duration, Member staff) throws SQLException {
        Databases.getMediaDatabase().update("""
                        INSERT INTO media_ranks(userid, duration, staffid) VALUES (?, ?, ?)
                        """,
                target.getIdLong(),
                duration,
                staff.getIdLong()
        );
    }

    public static void removeMemberRank(Member target) throws SQLException {
        Databases.getMediaDatabase().update("""
                DELETE FROM media_ranks WHERE userid = ?
                """,
                target.getId());
    }

    public static void blacklistMember(Member target,String reason, long duration, Member staff) throws SQLException {
        Databases.getMediaDatabase().update("""
                INSERT INTO media_blacklists(
                userid, reason, duration, staffid) VALUES (?, ?, ?, ?)
                """,
                target.getIdLong(),
                reason,
                duration,
                staff.getIdLong());
    }

    public static boolean isBlacklisted(Member target) {
        try {
            return Databases.getMediaDatabase().query(
                    "SELECT 1 FROM media_blacklists WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    target.getIdLong()
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void unblacklistMember(Member target) throws SQLException {
        Databases.getMediaDatabase().update("""
                DELETE FROM media_blacklists WHERE userid = ?
                """,
                target.getIdLong());
    }

    public static List<Long> getExpiredBlacklists() {
        long now = System.currentTimeMillis();

        try {
            return Databases.getMediaDatabase().query(
                    "SELECT userid FROM media_blacklists WHERE duration <= ?",
                    rs -> {
                        List<Long> expired = new ArrayList<>();
                        while (rs.next()) {
                            expired.add(rs.getLong("userid"));
                        }
                        return expired;
                    },
                    now
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static void removeBlacklistById(long userId) throws SQLException {
        Databases.getMediaDatabase().update(
                "DELETE FROM media_blacklists WHERE userid = ?",
                userId
        );
    }
}


/*
    /media-rank user:``` time: done by manager, admin: assign media role to a member for some time
    /media link:
    /media-blacklist user: time: => blacklists from usin /media cmd
    /media-unblacklist user: => unblacklists a blacklisted member

*/
