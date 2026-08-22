package com.dreggcake.src.PartnerFeatures.AutoPost;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getChannel().getId().equals(Utilities.PARTNER_QUEEN_CHANNEL.getId()))
            return;
    }
}

