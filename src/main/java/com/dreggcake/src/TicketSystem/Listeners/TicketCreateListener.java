package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.*;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;

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
                .addOption("Password Support", TicketType.PASSWORD.getId(), TicketEmojis.PASSWORD)
                .build();

        Utilities.TICKET_CATEGORY
                .createTextChannel("ticket-" + member.getUser().getName())
                .addPermissionOverride(
                        member.getGuild().getPublicRole(),
                        null,
                        EnumSet.of(Permission.VIEW_CHANNEL)
                )
                .addPermissionOverride(
                        member,
                        EnumSet.of(
                                Permission.VIEW_CHANNEL,
                                Permission.MESSAGE_ATTACH_FILES,
                                Permission.MESSAGE_ADD_REACTION,
                                Permission.MESSAGE_HISTORY
                        ),
                        null
                )
                .addPermissionOverride(
                        Utilities.STAFF_ROLE,
                        EnumSet.of(
                                Permission.VIEW_CHANNEL,
                                Permission.MESSAGE_SEND,
                                Permission.MESSAGE_ATTACH_FILES,
                                Permission.MESSAGE_ADD_REACTION,
                                Permission.MESSAGE_HISTORY
                        ),
                        null
                )
                .queue(channel -> {

                            channel.sendMessageEmbeds(embed.build())
                                    .addComponents(ActionRow.of(dropdown))
                                    .queue();


                            TicketDAO.insertTicket(channel, member);

                        }
                );
    }
}
