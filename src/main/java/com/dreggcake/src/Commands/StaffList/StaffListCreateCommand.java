package com.dreggcake.src.Commands.StaffList;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.StaffList.StaffListBuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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

        StaffListBuild messageBuilder = new StaffListBuild();
        List<MessageCreateBuilder> messageList = messageBuilder.buildList();

        for (MessageCreateBuilder message : messageList){
            event.getChannel().sendMessage(message.build()).useComponentsV2().queue();
        }

    }
}
