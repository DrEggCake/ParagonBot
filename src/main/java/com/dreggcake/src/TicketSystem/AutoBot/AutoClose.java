package com.dreggcake.src.TicketSystem.AutoBot;

import com.dreggcake.src.TicketSystem.TicketDAO;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoClose extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!TicketDAO.isTicket(event.getChannel().getIdLong()))
            return;

        String ticketMember = TicketDAO.retrieveTicketMemberId(event.getChannel().getIdLong());

        if(event.getAuthor().getId().equals(ticketMember)){

        }

    }
}
