/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src;

import com.dreggcake.src.Database.Databases;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.sql.Statement;

public class onSqlReady extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        try {
            createMainDatabase();
            createCountingDatabase();
            createMediaDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void createMainDatabase() throws SQLException {
        Databases.getMainDatabase().init();

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
            Statement stmt = Databases.getMainDatabase().createStatement();
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

    private void createCountingDatabase() throws SQLException {
        Databases.getCountingDatabase().init();

        String createCountingChannels = ("""
                    CREATE TABLE IF NOT EXISTS counting_channels (
                        channel_id INTEGER PRIMARY KEY,
                        current_number INTEGER NOT NULL,
                        last_user_id INTEGER NOT NULL,
                        contributions TEXT NOT NULL
                    )
                """);

        Statement stmt = Databases.getCountingDatabase().createStatement();
        stmt.execute(createCountingChannels);
        stmt.close();
    }

    private void createMediaDatabase() throws SQLException {
        Databases.getMediaDatabase().init();

        String createMediaRanks = """
                    CREATE TABLE IF NOT EXISTS media_ranks(
                        id INTEGER PRIMARY KEY,
                        userid INTEGER NOT NULL,
                        duration INTEGER NOT NULL,
                        staffid INTEGER NOT NULL
                    )
                """;

        String createMediaBlacklists = """
                    CREATE TABLE IF NOT EXISTS media_blacklists(
                        id INTEGER PRIMARY KEY,
                        userid INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        duration INTEGER NOT NULL,
                        staffid INTEGER NOT NULL
                    )
                """;

        Statement stmt = Databases.getMediaDatabase().createStatement();
        stmt.execute(createMediaRanks);
        stmt.execute(createMediaBlacklists);
        stmt.close();
    }

}