package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.TicketSystem.TicketCloser;
import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.Utility.GuildContext;
import com.dreggcake.src.Utility.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Periodic scan over all open tickets:
 * - silent tickets (no messages) are scheduled to auto-close 12h after 6h of silence
 * - conversations without any staff reply get a staff ping every 6h after 24h of silence
 * - tickets disabled via !autoClose are skipped
 * <p>
 * All state is persisted in the ticket_autoclose table.
 */
public class AutoCloseService implements Runnable {

    /* ================= Timings ================= */
    private static final long SILENCE_THRESHOLD = TimeUnit.HOURS.toMillis(6);
    private static final long CLOSE_HORIZON = TimeUnit.HOURS.toMillis(12);
    private static final long STAFF_IGNORE_THRESHOLD = TimeUnit.HOURS.toMillis(24);
    private static final long STAFF_PING_INTERVAL = TimeUnit.HOURS.toMillis(6);

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        Set<Long> openChannelIds = new HashSet<>();

        for (TicketDAO.OpenTicket ticket : TicketDAO.loadAllOpen()) {
            openChannelIds.add(ticket.channelId());

            TextChannel channel = GuildContext.JDA.getTextChannelById(ticket.channelId());
            if (channel == null) {
                AutoCloseDAO.delete(ticket.channelId());
                continue;
            }

            AutoCloseDAO.StateRow state = AutoCloseDAO.ensure(ticket.channelId(), ticket.createdAt());
            if (state.disabled()) continue;

            synchronizedState(channel, state, now);
        }

        cleanupOrphans(openChannelIds);
    }

    private void synchronizedState(TextChannel channel, AutoCloseDAO.StateRow state, long now) {
        if (!state.hasConversation()) {
            handleSilentTicket(channel, state, now);
        } else {
            handleUnansweredTicket(channel, state, now);
        }
    }

    /* ================= Rule 1: dead ticket ================= */

    private void handleSilentTicket(TextChannel channel, AutoCloseDAO.StateRow state, long now) {
        if (state.scheduledCloseAt() == null) {
            if (now - state.lastMessageAt() < SILENCE_THRESHOLD) return;

            AutoCloseDAO.save(withScheduled(state, now + CLOSE_HORIZON));

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Ticket scheduled for automatic closure")
                    .setDescription(
                            "This ticket had no activity for **6 hours**.\n" +
                                    "It will be closed " + TimeFormat.RELATIVE.format(now + CLOSE_HORIZON) + ".\n\n" +
                                    "*Sending any message will cancel this.*")
                    .setColor(Color.ORANGE)
                    .build()).queue();
            return;
        }

        if (now >= state.scheduledCloseAt()) {
            AutoCloseDAO.delete(channel.getIdLong());
            TicketCloser.close(channel, "AutoClose (no activity)",
                    "This ticket was closed automatically because it had no activity.", 10);
        }
    }

    /* ================= Rule 2: staff never answered ================= */

    private void handleUnansweredTicket(TextChannel channel, AutoCloseDAO.StateRow state, long now) {
        boolean userWaiting = state.lastUserMessageAt() > 0
                && now - state.lastUserMessageAt() >= STAFF_IGNORE_THRESHOLD
                && (state.lastStaffMessageAt() < 0 || state.lastStaffMessageAt() < state.lastUserMessageAt());

        if (!userWaiting || Roles.STAFF == null) return;
        if (now < state.nextStaffPingAt()) return;

        AutoCloseDAO.save(new AutoCloseDAO.StateRow(
                state.channelId(), state.disabled(), state.messageCount(), state.lastMessageAt(),
                state.lastUserMessageAt(), state.lastStaffMessageAt(), state.scheduledCloseAt(),
                now + STAFF_PING_INTERVAL));

        channel.sendMessage(Roles.STAFF.getAsMention())
                .addEmbeds(new EmbedBuilder()
                        .setTitle("Staff response needed")
                        .setDescription(
                                "This ticket has been waiting for a staff reply since " +
                                        TimeFormat.RELATIVE.format(state.lastUserMessageAt()) + ".\n" +
                                        "Staff will be reminded every **6 hours** until someone responds.")
                        .setColor(Color.YELLOW)
                        .build())
                .queue();
    }

    /* ================= Helpers ================= */

    /**
     * Removes state rows whose ticket no longer exists (closed manually etc.).
     */
    private void cleanupOrphans(Set<Long> openChannelIds) {
        for (AutoCloseDAO.StateRow row : AutoCloseDAO.loadAll()) {
            if (!openChannelIds.contains(row.channelId())) {
                AutoCloseDAO.delete(row.channelId());
            }
        }
    }

    private AutoCloseDAO.StateRow withScheduled(AutoCloseDAO.StateRow state, long closeAt) {
        return new AutoCloseDAO.StateRow(
                state.channelId(), state.disabled(), state.messageCount(), state.lastMessageAt(),
                state.lastUserMessageAt(), state.lastStaffMessageAt(), closeAt, state.nextStaffPingAt());
    }
}
