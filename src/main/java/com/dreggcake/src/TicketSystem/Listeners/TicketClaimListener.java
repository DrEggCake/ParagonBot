package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketButton;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketClaimListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.equals(TicketButton.CLAIM.getId())) return;

        Member member = event.getMember();
        if (member == null) return;

        if (!member.getRoles().contains(Utilities.STAFF_ROLE) &&
                !member.hasPermission(Permission.ADMINISTRATOR)) return;

        var message = event.getMessage();
        var embed = message.getEmbeds().get(0);

        boolean alreadyClaimed = embed.getFields().stream()
                .anyMatch(f -> f.getValue().equalsIgnoreCase(""));

        if (alreadyClaimed) {
            event.reply("This ticket is already claimed.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var builder = new EmbedBuilder(embed);
        builder.clearFields();
        builder.addField("Claimed by:", member.getAsMention(), false);

        var disabledButton = Button.primary(
                TicketButton.CLAIM.getId(),
                "Claimed"
        ).asDisabled();

        event.editMessageEmbeds(builder.build())
                .setComponents(ActionRow.of(disabledButton))
                .queue();
    }
}
