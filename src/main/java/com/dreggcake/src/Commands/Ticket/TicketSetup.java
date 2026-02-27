/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src.Commands.Ticket;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TicketSetup
        implements ICommand {
    @Override
    public String getName() {
        return "ticket-setup";
    }

    @Override
    public String getDescription() {
        return "Sets up the ticket system.";
    }

    @Override
    public List<OptionData> getOptions() {
        ArrayList<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "channel", "The ID of the channel to send the setup message", false));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if(!member.hasPermission(Permission.ADMINISTRATOR)){
            event.reply("Lol").setEphemeral(true).queue();
            return;
        }

        String channelId = event.getOption("channel") == null ? event.getChannelId() : event.getOption("channel").getAsString();
        TextChannel channel = event.getJDA().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("Invalid channel ID provided.").setEphemeral(true).queue();
            return;
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

        Button createTicketButton = Button.primary("create-ticket", "Create Ticket").withEmoji(Emoji.fromUnicode("\ud83c\udf9f\ufe0f"));
        event.reply("Ticket system setup complete.").setEphemeral(true).queue();
        channel.sendMessageEmbeds(embed.build(), new MessageEmbed[0]).setComponents(ActionRow.of(createTicketButton)).queue();
    }
}

