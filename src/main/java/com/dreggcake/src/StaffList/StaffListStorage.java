package com.dreggcake.src.StaffList;

import com.dreggcake.src.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffListStorage {

    public static void save(long guildId, String gamemode, long channelId, long messageId)
            throws SQLException {

        String sql = """
        INSERT INTO staff_list (guild_id, gamemode, channel_id, message_id)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(guild_id, gamemode)
        DO UPDATE SET channel_id = excluded.channel_id,
                      message_id = excluded.message_id
        """;

        try (PreparedStatement ps = Main.conn.prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setString(2, gamemode);
            ps.setLong(3, channelId);
            ps.setLong(4, messageId);
            ps.executeUpdate();
        }
    }

    public static Result get(long guildId, String gamemode) throws SQLException {
        String sql = """
        SELECT channel_id, message_id
        FROM staff_list
        WHERE guild_id = ? AND gamemode = ?
        """;

        try (PreparedStatement ps = Main.conn.prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setString(2, gamemode);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            return new Result(
                    rs.getLong("channel_id"),
                    rs.getLong("message_id")
            );
        }
    }

    public record Result(long channelId, long messageId) {}
}
