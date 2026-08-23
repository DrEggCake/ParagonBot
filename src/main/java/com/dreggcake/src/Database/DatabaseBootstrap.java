package com.dreggcake.src.Database;

import java.sql.SQLException;

/**
 * Entry point for database bootstrap: connects every database and ensures its schema exists.
 */
public final class DatabaseBootstrap {

    private DatabaseBootstrap() {
    }

    public static void initialize() throws SQLException {
        MainDatabaseSchema.init();
        CountingDatabaseSchema.init();
        MediaDatabaseSchema.init();
    }
}
