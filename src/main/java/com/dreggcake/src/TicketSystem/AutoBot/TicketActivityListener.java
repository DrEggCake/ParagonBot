package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.Utility.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

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

        if (AutoCloseDAO.clearScheduledClose(channelId)) {
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Automatic closure cancelled")
                    .setDescription("Someone is still here — the scheduled auto-close was cancelled.")
                    .setColor(Color.GREEN)
                    .build()).queue();
        }

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
