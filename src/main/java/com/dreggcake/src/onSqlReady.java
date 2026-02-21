/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.sql.Statement;

public class onSqlReady extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
//        TextChannel channel = event.getJDA().getTextChannelById("1251202767893037056");
//        channel.sendMessage("Restarted and ready to go").queue(msg -> msg.delete().queueAfter(100, TimeUnit.SECONDS));
        String createSqlmute = "CREATE TABLE IF NOT EXISTS mutes (id INTEGER PRIMARY KEY, userid text NOT NULL, reason TEXT NOT NULL, duration text NOT NULL, staffid text NOT NULL)";
        String createSqlban = "CREATE TABLE IF NOT EXISTS bans(id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)";
        String createSqlblacklistTicket = "CREATE TABLE IF NOT EXISTS blacklistTicket(id INTEGER PRIMARY KEY, userid TEXT NOT NULL,reason TEXT NOT NULL,duration TEXT NOT NULL,staffid TEXT NOT NULL)";
        String createSqltickets = "CREATE TABLE IF NOT EXISTS tickets (\n" +
                "    ticketid TEXT PRIMARY KEY,\n" +
                "    userid   TEXT NOT NULL UNIQUE,\n" +
                "    created_at INTEGER\n" +
                ");\n";
        String createSqlinvites = ("""
                    CREATE TABLE IF NOT EXISTS invites (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        inviter_id TEXT,
                        invited_id TEXT,
                        invite_code TEXT,
                        join_time INTEGER
                    )
                """);
        String createStaffList = ("""
                CREATE TABLE IF NOT EXISTS staff_list (
                    guild_id   TEXT NOT NULL,
                    gamemode   TEXT NOT NULL,
                    channel_id TEXT NOT NULL,
                    message_id TEXT NOT NULL,
                    PRIMARY KEY (guild_id, gamemode)
                );
                
                """);


        try {
            Statement stmt = Main.conn.createStatement();
            stmt.execute(createSqlmute);
            stmt.execute(createSqlban);
            stmt.execute(createSqlblacklistTicket);
            stmt.execute(createSqltickets);
            stmt.execute(createSqlinvites);
            stmt.execute(createStaffList);
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}