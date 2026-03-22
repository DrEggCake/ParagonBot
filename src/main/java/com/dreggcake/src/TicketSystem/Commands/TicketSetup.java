package com.dreggcake.src.TicketSystem.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.TicketSystem.TicketPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class TicketSetup implements ICommand {
    @Override
    public String getName() {
        return "ticket-setup";
    }

    @Override
    public String getDescription() {
        return "Setups up the Ticket System";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "channel", "Channel to setup in", false)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        Member member = event.getMember();
        if (member == null) return;

        if (!TicketPermissions.canSetup(member)) return;

        EmbedBuilder embedBuilder = new EmbedBuilder();





    }
}
