package com.dreggcake.src.TicketSystem.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Moderation.ModUtility;
import com.dreggcake.src.TicketSystem.TicketDAO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class RenameTicket implements ICommand {
    @Override
    public String getName() {
        return "rename-ticket";
    }

    @Override
    public String getDescription() {
        return "Rename the current ticket";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "name", "The new name of ticket", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();

        if (executer.getRoles().stream().noneMatch(ModUtility.getRenameTicketStaff()::contains)) {
            event.reply("You do not have permission.").setEphemeral(true).queue();
            return;
        }

        if (!event.getChannelType().isMessage())
            return;

        if (!TicketDAO.isTicket(event.getChannelIdLong())) {
            event.reply("Not a ticket").setEphemeral(true).queue();
            return;
        }

        String newName = event.getOption("name").getAsString();

        event.deferReply().queue(
                hook -> {
                    event.getChannel()
                            .asTextChannel()
                            .getManager()
                            .setName(newName)
                            .queue(
                                    success -> hook.editOriginal("Ticket renamed to: " + newName).queue(),
                                    error -> hook.editOriginal("Something went wrong!").queue()
                            );
                }
        );

    }
}
