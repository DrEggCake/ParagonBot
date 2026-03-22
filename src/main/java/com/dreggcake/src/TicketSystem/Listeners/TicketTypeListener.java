package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.TicketSystem.TicketType;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketTypeListener extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        event.deferEdit().queue();

        long channelIdLong = event.getChannelIdLong();
        Member member = event.getMember();

        long ticketMember = Long.parseLong(TicketDAO.retrieveTicketMemberId(channelIdLong));

        if (member.getIdLong() != ticketMember) {
            event.reply("Not your ticket!").queue();
            return;
        }

        TicketType ticketType = TicketType.fromId(event.getValues().get(0));

        if (ticketType == null) return;

        ticketType.apply(event.getChannel().asTextChannel(), event.getMember());

        event.getMessage().delete().queue();

        event.getChannel().sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(
                message -> {
                    message.delete().queue();
                }
        );

        event.getChannel().sendMessage(event.getMember().getAsMention()).queue(
                message -> {
                    message.delete().queue();
                }
        );

    }
}
