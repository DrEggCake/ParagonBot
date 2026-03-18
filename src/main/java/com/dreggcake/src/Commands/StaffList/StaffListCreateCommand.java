package com.dreggcake.src.Commands.StaffList;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.StaffList.StaffMessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.container.Container;
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
        if(!executer.hasPermission(Permission.ADMINISTRATOR)){
            event.reply("Lol").setEphemeral(true).queue();
            return;
        }

        event.reply("Creating staff list...").setEphemeral(true).queue();

        StaffMessageBuilder messageBuilder = new StaffMessageBuilder();
//        event.getChannel().sendMessageComponents(messageBuilder.build(event.getGuild()).withAccentColor(Color.BLUE)).useComponentsV2().queue();

        for(Container container : messageBuilder.buildAllMessages(event.getGuild())){
            event.getChannel().sendMessageComponents(container.withAccentColor(Color.BLUE)).useComponentsV2().queue();
        }
    }
}
