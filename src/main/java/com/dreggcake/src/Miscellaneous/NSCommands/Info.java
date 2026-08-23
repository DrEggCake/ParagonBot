package com.dreggcake.src.Miscellaneous.NSCommands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Info extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String cmd = "!info";
        if (!event.getMessage().getContentRaw().equals(cmd))
            return;


    }
}
