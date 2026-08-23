package com.dreggcake.src.TicketSystem.AutoBot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-RAM activity state per open ticket channel. Fed by live message events,
 * seeded from the database for tickets that predate the bot session.
 */
final class TicketActivityTracker {

    /**
     * Activity snapshot of one ticket channel.
     */
    static final class Activity {
        long createdAt;
        long lastMessageAt;
        long lastUserMessageAt = -1;
        long lastStaffMessageAt = -1;
        int messageCount;
        Long scheduledCloseAt;
        long nextStaffPingAt;

        Activity(long createdAt) {
            this.createdAt = createdAt;
            this.lastMessageAt = createdAt;
        }

        boolean hasConversation() {
            return messageCount > 0;
        }
    }

    private final Map<Long, Activity> activities = new ConcurrentHashMap<>();

    Activity getOrCreate(long channelId, long createdAtFallback) {
        return activities.computeIfAbsent(channelId, id -> new Activity(createdAtFallback));
    }

    void record(long channelId, boolean staff, long at) {
        Activity a = getOrCreate(channelId, at);
        synchronized (a) {
            a.messageCount++;
            a.lastMessageAt = at;
            if (staff) {
                a.lastStaffMessageAt = at;
            } else {
                a.lastUserMessageAt = at;
            }
            // A new message proves the ticket is alive again
            a.scheduledCloseAt = null;
            a.nextStaffPingAt = 0;
        }
    }

    /**
     * Cancels a pending auto-close because someone wrote in the ticket.
     *
     * @return true if a schedule was actually cancelled
     */
    boolean cancelScheduledClose(long channelId) {
        Activity a = activities.get(channelId);
        if (a == null) return false;
        synchronized (a) {
            if (a.scheduledCloseAt == null) return false;
            a.scheduledCloseAt = null;
            return true;
        }
    }

    void remove(long channelId) {
        activities.remove(channelId);
    }
}
