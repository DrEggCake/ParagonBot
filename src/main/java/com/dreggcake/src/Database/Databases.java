package com.dreggcake.src.Database;

import com.dreggcake.src.Config.Config;

public class Databases {

    private static final DAO mainDAO = new DAO(
            Config.getString("database.main", "")
    );

    private static final DAO countingDAO = new DAO(
            Config.getString("database.counting", "")
    );

    private static final DAO mediaDAO = new DAO(
            Config.getString("database.media", "")
    );


    public static DAO getMainDatabase() {
        return mainDAO;
    }

    public static DAO getCountingDatabase() {
        return countingDAO;
    }

    public static DAO getMediaDatabase() {
        return mediaDAO;
    }
}
