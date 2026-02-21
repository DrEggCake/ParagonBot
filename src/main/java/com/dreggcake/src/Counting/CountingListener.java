package com.dreggcake.src.Counting;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CountingListener extends ListenerAdapter {

    private final CountingService service = new CountingService();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;
        if (!(event.getChannel() instanceof TextChannel channel)) return;

        CountingState state = CountingManager.get(channel.getIdLong());
        if (state == null) return;

        service.handle(event, channel, state);
    }
}
