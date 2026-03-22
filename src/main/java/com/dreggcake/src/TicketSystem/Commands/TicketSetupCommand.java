package com.dreggcake.src.TicketSystem.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.TicketSystem.TicketButton;
import com.dreggcake.src.TicketSystem.TicketPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;

public class TicketSetupCommand implements ICommand {
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

        MessageChannel channel;
        var option = event.getOption("channel");
        if (option == null) {
            channel = event.getChannel();
        } else {
            channel = option.getAsChannel().asGuildMessageChannel();
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(":tickets: Support Tickets");
        embed.setDescription("Welcome to the Official Support Center of **Paragon Network**\n" +
                "Please open a ticket by clicking the button below!\n\n" +
                "### :warning: PLEASE NOTE: \n" +
                "When opening a ticket please just don't say \"hi\" or \"hello\" then disappear, try explaining your problem in a well written message.\n\n " +
                "We try to maintain a short response time, but keep in mind our staff members aren't robots, please be patient! \n" +
                "You will get a response as soon as possible.");
        embed.setFooter("ParagonMC Ticket System");
        embed.setColor(Color.BLUE);

        Button createTicketButton = Button.primary(TicketButton.CREATE.getId(), "Create Ticket").withEmoji(Emoji.fromUnicode("\ud83c\udf9f\ufe0f"));
        event.reply("Ticket system setup complete.").setEphemeral(true).queue();
        channel.sendMessageEmbeds(embed.build(), new MessageEmbed[0]).setComponents(ActionRow.of(createTicketButton)).queue();

    }
}
