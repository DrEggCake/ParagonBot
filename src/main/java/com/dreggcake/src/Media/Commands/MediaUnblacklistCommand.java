package com.dreggcake.src.Media.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Media.Logic.MediaDAO;
import com.dreggcake.src.Media.MediaPermissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.Roles;

public class MediaUnblacklistCommand implements ICommand
{
    @Override
    public String getName() {
        return "media-unblacklist";
    }

    @Override
    public String getDescription() {
        return "Unblacklist";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "User to unblacklist", true),
                new OptionData(OptionType.STRING, "reason", "Reason to unblacklist", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            return;
        }
        Member member = event.getMember();
        if (!MediaPermissions.canManagePermission(member)) {
            event.reply("No permission").setEphemeral(true).queue();
            return;
        }

        Member target = event.getOption("user").getAsMember();
        String reason = event.getOption("reason").getAsString();

        if (target.getRoles().contains(Roles.STAFF) || target.isOwner() || target.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("what do you think you are doing weirdo?").setEphemeral(true).queue();
            return;
        }


        if (!MediaDAO.isBlacklisted(target)) {
            event.reply("Target is not blacklisted!").setEphemeral(true).queue();
            return;
        }
        MediaDAO.unblacklistMember(target.getIdLong());
        event.reply(target.getAsMention() + " has been unblacklisted").queue();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Member Media Unblacklisted");
        embed.addField("Staff", event.getMember().getAsMention(), true);
        embed.addField("Target", target.getAsMention(), true);
        embed.addField("Reason", reason, false);
        embed.setColor(Color.YELLOW);
        Channels.MEDIA_LOG.sendMessageEmbeds(embed.build()).queue();
    }
}
