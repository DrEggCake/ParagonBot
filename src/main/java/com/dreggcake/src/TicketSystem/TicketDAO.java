package com.dreggcake.src.TicketSystem;

import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;

public class TicketDAO {


    public static void insertTicket(TextChannel channel, Member member) {
        try {
            Databases.getMainDatabase().update("""
                            INSERT INTO tickets (
                                ticketid, userid, created_at )
                                VALUES (?, ?, ?)
                            """,
                    channel.getId(),
                    member.getId(),
                    System.currentTimeMillis()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTicket(long ticketId) {
        try {
            Databases.getMainDatabase().update("""
                            DELETE FROM tickets WHERE ticketid = ?
                            """,
                    ticketId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String retrieveTicketMemberId(long ticketId) {
        try {
            return Databases.getMainDatabase().query(
                    "SELECT userid FROM tickets WHERE ticketid = ?",
                    rs -> rs.next() ? rs.getString("userid") : null,
                    ticketId
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isTicket(long channelId) {
        try {
            return Databases.getMainDatabase().query(
                    "SELECT 1 FROM tickets WHERE ticketid = ?",
                    rs -> rs.next(),
                    channelId
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}