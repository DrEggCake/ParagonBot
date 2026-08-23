package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.TicketSystem.TicketCloser;
import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.Utility.GuildContext;
import com.dreggcake.src.Utility.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * Periodic scan over all open tickets:
 * - silent tickets (no messages) are scheduled to auto-close 12h after 6h of silence
 * - conversations without any staff reply get a staff ping every 6h after 24h of silence
 */
public class AutoCloseService implements Runnable {

    /* ================= Timings ================= */
    private static final long SILENCE_THRESHOLD = TimeUnit.HOURS.toMillis(6);
    private static final long CLOSE_HORIZON = TimeUnit.HOURS.toMillis(12);
    private static final long STAFF_IGNORE_THRESHOLD = TimeUnit.HOURS.toMillis(24);
    private static final long STAFF_PING_INTERVAL = TimeUnit.HOURS.toMillis(6);

    private static boolean enabled = true;

    private static final AutoCloseService INSTANCE = new AutoCloseService();

    private final TicketActivityTracker tracker = new TicketActivityTracker();

    public static AutoCloseService get() {
        return INSTANCE;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    TicketActivityTracker tracker() {
        return tracker;
    }

    void recordActivity(long channelId, boolean staff) {
        tracker.record(channelId, staff, System.currentTimeMillis());
    }

    @Override
    public void run() {
        if (!enabled) return;

        long now = System.currentTimeMillis();

        for (TicketDAO.OpenTicket ticket : TicketDAO.loadAllOpen()) {
            TextChannel channel = GuildContext.JDA.getTextChannelById(ticket.channelId());
            if (channel == null) {
                tracker.remove(ticket.channelId());
                continue;
            }

            TicketActivityTracker.Activity activity =
                    tracker.getOrCreate(ticket.channelId(), ticket.createdAt());

            synchronized (activity) {
                if (!activity.hasConversation()) {
                    handleSilentTicket(channel, activity, now);
                } else {
                    handleUnansweredTicket(channel, activity, now);
                }
            }
        }
    }

    /* ================= Rule 1: dead ticket ================= */

    private void handleSilentTicket(TextChannel channel, TicketActivityTracker.Activity activity, long now) {
        if (activity.scheduledCloseAt == null) {
            if (now - activity.lastMessageAt < SILENCE_THRESHOLD) return;

            activity.scheduledCloseAt = now + CLOSE_HORIZON;

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Ticket scheduled for automatic closure")
                    .setDescription(
                            "This ticket had no activity for **6 hours**.\n" +
                                    "It will be closed " + TimeFormat.RELATIVE.format(activity.scheduledCloseAt) + ".\n\n" +
                                    "*Sending any message will cancel this.*")
                    .setColor(Color.ORANGE)
                    .build()).queue();
            return;
        }

        if (now >= activity.scheduledCloseAt) {
            long channelId = channel.getIdLong();
            tracker.remove(channelId);
            TicketCloser.close(channel, "AutoClose (no activity)",
                    "This ticket was closed automatically because it had no activity.", 10);
        }
    }

    /* ================= Rule 2: staff never answered ================= */

    private void handleUnansweredTicket(TextChannel channel, TicketActivityTracker.Activity activity, long now) {
        boolean userWaiting = activity.lastUserMessageAt > 0
                && now - activity.lastUserMessageAt >= STAFF_IGNORE_THRESHOLD
                && (activity.lastStaffMessageAt < 0 || activity.lastStaffMessageAt < activity.lastUserMessageAt);

        if (!userWaiting || Roles.STAFF == null) return;
        if (now < activity.nextStaffPingAt) return;

        activity.nextStaffPingAt = now + STAFF_PING_INTERVAL;

        channel.sendMessage(Roles.STAFF.getAsMention())
                .addEmbeds(new EmbedBuilder()
                        .setTitle("Staff response needed")
                        .setDescription(
                                "This ticket has been waiting for a staff reply since " +
                                        TimeFormat.RELATIVE.format(activity.lastUserMessageAt) + ".\n" +
                                        "Staff will be reminded every **6 hours** until someone responds.")
                        .setColor(Color.YELLOW)
                        .build())
                .queue();
    }
}
