package com.dreggcake.src.Database;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the tables of the media SQLite database if they do not exist yet.
 */
final class MediaDatabaseSchema {

    private MediaDatabaseSchema() {
    }

    static void init() throws SQLException {
        DAO db = Databases.getMediaDatabase();
        db.init();

        try (Statement stmt = db.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS media_ranks(
                    id INTEGER PRIMARY KEY,
                    userid INTEGER NOT NULL,
                    duration INTEGER NOT NULL,
                    staffid INTEGER NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS media_blacklists(
                    id INTEGER PRIMARY KEY,
                    userid INTEGER NOT NULL,
                    reason TEXT NOT NULL,
                    staffid INTEGER NOT NULL
                )
            """);
        }
    }
}
