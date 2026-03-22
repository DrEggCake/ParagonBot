package com.dreggcake.src.TicketSystem.Commands;

import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
        return List.of();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }
}
