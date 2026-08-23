package com.dreggcake.src.Database;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the tables of the counting SQLite database if they do not exist yet.
 */
final class CountingDatabaseSchema {

    private CountingDatabaseSchema() {
    }

    static void init() throws SQLException {
        DAO db = Databases.getCountingDatabase();
        db.init();

        try (Statement stmt = db.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS counting_channels (
                    channel_id INTEGER PRIMARY KEY,
                    current_number INTEGER NOT NULL,
                    last_user_id INTEGER NOT NULL,
                    contributions TEXT NOT NULL
                )
            """);
        }
    }
}
