package com.dreggcake.src.Media.Logic;


import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;

public class MediaDAO {

    public static void addMemberRank(Member target, long duration, Member staff) {
        try {
            Databases.getMediaDatabase().update("""
                            INSERT INTO media_ranks(userid, duration, staffid) VALUES (?, ?, ?)
                            """,
                    target.getIdLong(),
                    duration,
                    staff.getIdLong()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeMemberRank(Member target) {
        try {
            Databases.getMediaDatabase().update("""
                            DELETE FROM media_ranks WHERE userid = ?
                            """,
                    target.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void blacklistMember(Member target, String reason, Member staff) {
        try {
            Databases.getMediaDatabase().update("""
                            INSERT INTO media_blacklists(
                            userid, reason, staffid) VALUES (?, ?, ?)
                            """,
                    target.getIdLong(),
                    reason,
                    staff.getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public static boolean isMember(Member target) {
        try {
            return Databases.getMediaDatabase().query(
                    "SELECT 1 FROM media_ranks WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    target.getIdLong()
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void unblacklistMember(Member target) {
        try {
            Databases.getMediaDatabase().update("""
                            DELETE FROM media_blacklists WHERE userid = ?
                            """,
                    target.getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void unblacklistMember(long userId) {
        try {
            Databases.getMediaDatabase().update(
                    "DELETE FROM media_blacklists WHERE userid = ?",
                    userId
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


/*
    /media-rank user:``` time: done by manager, admin: assign media role to a member for some time
    /media link:
    /media-blacklist user: time: => blacklists from usin /media cmd
    /media-unblacklist user: => unblacklists a blacklisted member

*/
