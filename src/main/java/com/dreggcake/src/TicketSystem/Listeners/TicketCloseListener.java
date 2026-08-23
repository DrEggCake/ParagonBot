package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketCloser;
import com.dreggcake.src.TicketSystem.TicketDAO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.dreggcake.src.Utility.Roles;

public class TicketCloseListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() == null) return;

        String content = event.getMessage().getContentRaw();

        if (content.equals("!close")) {
            if (!(event.getChannel() instanceof TextChannel)) return;

            TextChannel channel = event.getChannel().asTextChannel();

            Member member = event.getMember();

            boolean isStaff = member.getRoles().contains(Roles.STAFF);
            boolean isAdmin = member.hasPermission(Permission.ADMINISTRATOR);

            if (!(isStaff || isAdmin)) return;

            TicketCloser.close(channel, member,
                    "Ticket will be deleted in 10 seconds, thank you for reaching out!\n" +
                            "Have a good day! :heart:", 10);
        }

    }
}
