package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.TicketSystem.TicketData;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketCloseListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() == null) return;

        String content = event.getMessage().getContentRaw();

        if(content.equals("!close")){
            if(!(event.getChannel() instanceof TextChannel)) return;

            TextChannel channel = event.getChannel().asTextChannel();
            TicketData ticketData = TicketDAO.retrieveTicket(channel.getIdLong());

            Member member = event.getMember();

            if(member.getIdLong() != ticketData.userId() ||
                    !member.getRoles().contains(Utilities.STAFF_ROLE) ||
                    !member.hasPermission(Permission.ADMINISTRATOR)
            ) return;














        }


    }
}
