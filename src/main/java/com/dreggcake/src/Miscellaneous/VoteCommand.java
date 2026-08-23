package com.dreggcake.src.Miscellaneous;

import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * /vote — slash variant of the vote links command.
 * Registered through the CommandManager like every other ICommand.
 */
public class VoteCommand implements ICommand {

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public String getDescription() {
        return "Get all voting links and support the server!";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(VoteEmbeds.embed())
                .addComponents(VoteEmbeds.rows())
                .queue();
    }
}
