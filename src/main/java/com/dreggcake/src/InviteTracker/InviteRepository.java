package com.dreggcake.src.InviteTracker;

import com.dreggcake.src.Database.Databases;

import java.sql.SQLException;

/**
 * Persistence of invite attributions.
 */
public class InviteRepository {

    public void save(String inviterId, String invitedId, String code) {
        try {
            Databases.getMainDatabase().update("INSERT INTO invites (" +
                    "inviter_id, invited_id, invite_code, join_time" +
                    ") VALUES (?, ?, ?, ?)", inviterId, invitedId, code, System.currentTimeMillis());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
