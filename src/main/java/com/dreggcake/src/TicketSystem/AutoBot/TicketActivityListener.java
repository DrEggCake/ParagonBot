package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.Utility.Roles;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Feeds live ticket messages into the persisted AutoClose state and
 * announces cancelled auto-closes.
 */
public class TicketActivityListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Member member = event.getMember();
        if (member == null) return;

        long channelId = event.getChannel().getIdLong();
        if (!TicketDAO.isTicket(channelId)) return;

        long now = System.currentTimeMillis();

        // Activity detected: silently cancel any pending auto-close (no announcement)
        AutoCloseDAO.clearScheduledClose(channelId);

        AutoCloseDAO.StateRow state = AutoCloseDAO.ensure(channelId, now);
        boolean staff = member.getRoles().contains(Roles.STAFF);

        AutoCloseDAO.save(new AutoCloseDAO.StateRow(
                channelId,
                state.disabled(),
                state.messageCount() + 1,
                now,
                staff ? state.lastUserMessageAt() : now,
                staff ? now : state.lastStaffMessageAt(),
                state.scheduledCloseAt(),
                0
        ));
    }
}
