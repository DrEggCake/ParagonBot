package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.Utility.PermissionUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Toggles AutoClose for a single ticket channel with !autoClose.
 * Run inside a ticket to exempt it from auto closure (e.g. when you know
 * a longer wait is expected); run again to re-enable it.
 */
public class AutoCloseToggle extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null || event.getAuthor().isBot()) return;

        if (!event.getMessage().getContentStripped().equalsIgnoreCase("!autoClose")) return;

        if (!PermissionUtility.canManageConfig(member)) {
            event.getMessage().reply("You do not have permission to toggle auto close.").queue();
            return;
        }

        long channelId = event.getChannel().getIdLong();
        if (!TicketDAO.isTicket(channelId)) {
            event.getMessage().reply("`!autoClose` only works inside a ticket channel.").queue();
            return;
        }

        AutoCloseDAO.StateRow state = AutoCloseDAO.ensure(channelId, System.currentTimeMillis());

        if (state.disabled()) {
            AutoCloseDAO.save(state.withDisabled(false, System.currentTimeMillis()));
            event.getMessage().reply("Auto close is now **ENABLED** for this ticket.").queue();
        } else {
            AutoCloseDAO.save(state.withDisabled(true, System.currentTimeMillis()));
            event.getMessage()
                    .reply("Auto close is now **DISABLED** for this ticket — it will not be closed automatically.")
                    .queue();
        }
    }
}
