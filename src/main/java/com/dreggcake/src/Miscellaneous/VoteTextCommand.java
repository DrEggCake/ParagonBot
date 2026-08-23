package com.dreggcake.src.Miscellaneous;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * !vote — text variant of the /vote command.
 */
public class VoteTextCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.getMessage().getContentStripped().equalsIgnoreCase("!vote")) return;

        event.getChannel().sendMessageEmbeds(VoteEmbeds.embed())
                .addComponents(ActionRow.of(VoteEmbeds.buttons()))
                .queue();
    }
}
