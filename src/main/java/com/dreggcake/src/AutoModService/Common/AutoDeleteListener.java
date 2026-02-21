package com.dreggcake.src.AutoModService.Common;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Set;

public class AutoDeleteListener extends ListenerAdapter {

    private static final Set<String> NOT_ALLOWED_WORDS = Set.of(
            "twenture",
            "twent"
    );

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getMember() == null) return;
        if (event.getMember().getUser().isBot()) return;
        if (event.getMember().isOwner()) return;
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;

        String content = event.getMessage().getContentStripped().toLowerCase();
        if (NOT_ALLOWED_WORDS.contains(content)) {
            event.getMessage().delete().queue();
        }
    }
}
