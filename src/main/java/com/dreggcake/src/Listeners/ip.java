package com.dreggcake.src.Listeners;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class ip extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        if(msg.equalsIgnoreCase("!ip")){
            sendip(event.getMessage(), event);
        }
    }


    private void sendip(Message message, MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(":video_game: Server IP");
        embed.setDescription(":desktop: Java: **"+Utilities.IP+"**");
        embed.setColor(Color.BLUE);
        embed.setFooter("Paragon Network | Auto Response",event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        message.replyEmbeds(embed.build()).queue();
    }
}
