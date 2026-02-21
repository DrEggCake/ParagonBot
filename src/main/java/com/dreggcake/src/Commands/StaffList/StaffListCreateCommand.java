package com.dreggcake.src.Commands.StaffList;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.StaffList.StaffMessageBuilder;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;

public class StaffListCreateCommand implements ICommand {
    @Override
    public String getName() {
        return "stafflist-create";
    }

    @Override
    public String getDescription() {
        return "Creates and sends staff list";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        if (!(executer.getRoles().contains(Utilities.MANAGER_ROLE) || executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.MANAGER_ROLE) || role.getPosition() >= Utilities.MANAGER_ROLE.getPosition()))) {
            event.reply("No permissions").setEphemeral(true).queue();
        }
        event.reply("Done!").setEphemeral(true).queue();

        StaffMessageBuilder messageBuilder = new StaffMessageBuilder();
        event.getChannel().sendMessageComponents(messageBuilder.build(event.getGuild()).withAccentColor(Color.BLUE)).useComponentsV2().queue();
    }
}
