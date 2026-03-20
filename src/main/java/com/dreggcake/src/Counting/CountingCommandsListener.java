package com.dreggcake.src.Counting;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CountingCommandsListener extends ListenerAdapter {

    private final CountingCommands commands = new CountingCommands();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        commands.handle(event);
    }
}
