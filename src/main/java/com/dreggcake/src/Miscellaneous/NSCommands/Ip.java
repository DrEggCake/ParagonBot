package com.dreggcake.src.Miscellaneous.NSCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.RuntimeValues;

public class Ip extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getCategory() == Channels.TICKET_CATEGORY)
            return;
        String msg = event.getMessage().getContentRaw();
        if(msg.equalsIgnoreCase("!ip") || msg.equalsIgnoreCase("ip?")){
            sendip(event.getMessage(), event);
        }
    }


    private void sendip(Message message, MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(":video_game: Server IP");
        embed.setDescription(":desktop: Java: **"+RuntimeValues.IP+"**");
        embed.setColor(Color.BLUE);
        embed.setFooter("Paragon Network | Auto Response",event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        message.replyEmbeds(embed.build()).queue();
    }
}
