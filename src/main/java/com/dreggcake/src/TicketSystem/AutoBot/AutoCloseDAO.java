package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.Database.Databases;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistence of the per-ticket AutoClose state (survives restarts).
 */
public final class AutoCloseDAO {

    /**
     * AutoClose state of one ticket channel.
     */
    public record StateRow(
            long channelId,
            boolean disabled,
            int messageCount,
            long lastMessageAt,
            long lastUserMessageAt,
            long lastStaffMessageAt,
            Long scheduledCloseAt,
            long nextStaffPingAt
    ) {
        static StateRow fresh(long channelId, long createdAt) {
            return new StateRow(channelId, false, 0, createdAt, -1, -1, null, 0);
        }

        boolean hasConversation() {
            return messageCount > 0;
        }

        StateRow withDisabled(boolean newDisabled, long now) {
            // Re-enabling starts a fresh silence window so the ticket is not closed instantly
            if (newDisabled) {
                return new StateRow(channelId, true, messageCount, lastMessageAt, lastUserMessageAt, lastStaffMessageAt, null, 0);
            }
            return new StateRow(channelId, false, messageCount, now, now, -1, null, 0);
        }
    }

    private AutoCloseDAO() {
    }

    public static StateRow get(long channelId) {
        try {
            return Databases.getMainDatabase().query(
                    "SELECT * FROM ticket_autoclose WHERE channel_id = ?",
                    rs -> rs.next() ? map(rs) : null,
                    channelId
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return StateRow.fresh(channelId, System.currentTimeMillis());
        }
    }

    /**
     * Creates the row for a ticket if it does not exist yet.
     *
     * @param createdAtFallback seed for last activity when the row is new
     */
    public static StateRow ensure(long channelId, long createdAtFallback) {
        StateRow row = get(channelId);
        if (row != null) return row;

        StateRow fresh = StateRow.fresh(channelId, createdAtFallback);
        save(fresh);
        return fresh;
    }

    public static void save(StateRow row) {
        try {
            Databases.getMainDatabase().update("""
                            INSERT INTO ticket_autoclose (
                                channel_id, disabled, message_count, last_message_at,
                                last_user_message_at, last_staff_message_at,
                                scheduled_close_at, next_staff_ping_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            ON CONFLICT(channel_id) DO UPDATE SET
                                disabled = excluded.disabled,
                                message_count = excluded.message_count,
                                last_message_at = excluded.last_message_at,
                                last_user_message_at = excluded.last_user_message_at,
                                last_staff_message_at = excluded.last_staff_message_at,
                                scheduled_close_at = excluded.scheduled_close_at,
                                next_staff_ping_at = excluded.next_staff_ping_at
                            """,
                    row.channelId(),
                    row.disabled() ? 1 : 0,
                    row.messageCount(),
                    row.lastMessageAt(),
                    row.lastUserMessageAt(),
                    row.lastStaffMessageAt(),
                    row.scheduledCloseAt(),
                    row.nextStaffPingAt()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if a pending schedule existed and was cleared
     */
    public static boolean clearScheduledClose(long channelId) {
        StateRow row = get(channelId);
        if (row == null || row.scheduledCloseAt() == null) return false;

        save(new StateRow(channelId, row.disabled(), row.messageCount(), row.lastMessageAt(),
                row.lastUserMessageAt(), row.lastStaffMessageAt(), null, row.nextStaffPingAt()));
        return true;
    }

    public static void delete(long channelId) {
        try {
            Databases.getMainDatabase().update("DELETE FROM ticket_autoclose WHERE channel_id = ?", channelId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<StateRow> loadAll() {
        try {
            return Databases.getMainDatabase().query(
                    "SELECT * FROM ticket_autoclose",
                    rs -> {
                        List<StateRow> list = new ArrayList<>();
                        while (rs.next()) list.add(map(rs));
                        return list;
                    }
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static StateRow map(java.sql.ResultSet rs) throws SQLException {
        long scheduled = rs.getLong("scheduled_close_at");
        boolean hasScheduled = !rs.wasNull();

        return new StateRow(
                rs.getLong("channel_id"),
                rs.getInt("disabled") == 1,
                rs.getInt("message_count"),
                rs.getLong("last_message_at"),
                rs.getLong("last_user_message_at"),
                rs.getLong("last_staff_message_at"),
                hasScheduled ? scheduled : null,
                rs.getLong("next_staff_ping_at")
        );
    }
}
