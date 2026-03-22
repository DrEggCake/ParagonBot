package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketButton;
import com.dreggcake.src.TicketSystem.TicketEmojis;
import com.dreggcake.src.TicketSystem.TicketPermissions;
import com.dreggcake.src.TicketSystem.TicketType;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.print.DocFlavor;
import java.awt.*;

public class TicketCreateListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        Member member = event.getMember();

        TicketButton button = TicketButton.fromId(id);

        if (button == null) return;

        if (button != TicketButton.CREATE) return;

        if (TicketPermissions.isBlacklisted(member)) {
            event.reply("You are blacklisted!").queue();
            return;
        }

        createTicket(member);


    }

    private void createTicket(Member member) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Support Type");
        embed.setDescription("Please select the type of support you need.");
        embed.addField("User", member.getAsMention(), false);
        embed.setColor(Color.YELLOW);


        StringSelectMenu dropdown = StringSelectMenu.create(TicketType.TICKET_TYPE.getId())
                .setPlaceholder("Select a Support Type")
                .addOption("General Support", TicketType.GENERAL.getId(), TicketEmojis.GENERAL)
                .addOption("Appeal Support", TicketType.APPEAL.getId(), TicketEmojis.APPEAL)
                .addOption("Report Player", TicketType.REPORT.getId(), TicketEmojis.REPORT)
                .addOption("Bug Report", TicketType.BUG.getId(), TicketEmojis.BUG)
                .addOption("Store Support", TicketType.STORE.getId(), TicketEmojis.SHOP)
                .build();


        Utilities.TICKET_CATEGORY.createTextChannel("ticket-" + member.getUser().getName()).queue(
                channel -> {


                    channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(dropdown)).queue();


                    channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(
                            message -> {
                                message.delete().queue();
                            }
                    );
                    channel.sendMessage(member.getAsMention()).queue(
                            message -> {
                                message.delete().queue();
                            }
                    );
                }
        );








    }
}
