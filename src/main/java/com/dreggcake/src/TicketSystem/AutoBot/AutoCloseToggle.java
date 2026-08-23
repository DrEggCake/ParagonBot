package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.Utility.PermissionUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Toggles the AutoClose feature with !autoClose.
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

        boolean nowEnabled = AutoCloseService.toggle();
        event.getMessage().reply("Auto close is now **" + (nowEnabled ? "ENABLED" : "DISABLED") + "**.").queue();
    }
}
