package com.dreggcake.src.Miscellaneous.NSCommands;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Store extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        if (msg.equalsIgnoreCase("!store")) {
            sendStore(event.getMessage());
        }
    }

    private void sendStore(Message message) {

        message.reply("Hello there! Our webstore is right here: "
                        + Utilities.STORE
                        + "\nHappy shopping! :shopping_bags:")
                .setSuppressEmbeds(true)
                .queue();
    }
}
