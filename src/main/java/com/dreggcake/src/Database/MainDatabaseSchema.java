package com.dreggcake.src.Database;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the tables of the main SQLite database if they do not exist yet.
 */
final class MainDatabaseSchema {

    private MainDatabaseSchema() {
    }

    static void init() throws SQLException {
        DAO db = Databases.getMainDatabase();
        db.init();

        try (Statement stmt = db.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS mutes (id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS bans(id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS blacklistTicket(id INTEGER PRIMARY KEY, userid TEXT NOT NULL, reason TEXT NOT NULL, duration TEXT NOT NULL, staffid TEXT NOT NULL)");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tickets (
                    ticketid TEXT PRIMARY KEY,
                    userid TEXT NOT NULL,
                    created_at INTEGER
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS invites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    inviter_id TEXT,
                    invited_id TEXT,
                    invite_code TEXT,
                    join_time INTEGER
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS staff_list (
                    guild_id TEXT NOT NULL,
                    gamemode TEXT NOT NULL,
                    channel_id TEXT NOT NULL,
                    message_id TEXT NOT NULL,
                    PRIMARY KEY (guild_id, gamemode)
                )
            """);

        }
    }
}
