package com.dreggcake.src.Media.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Media.Logic.MediaDAO;
import com.dreggcake.src.Media.MediaPermissions;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

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

        if (target.getRoles().contains(Utilities.STAFF_ROLE) || target.isOwner() || target.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("what do you think you are doing weirdo?").setEphemeral(true).queue();
            return;
        }


        if (!MediaDAO.isBlacklisted(target)) {
            event.reply("Target is not blacklisted!").setEphemeral(true).queue();
            return;
        }
        MediaDAO.unblacklistMember(target.getIdLong());
        event.reply(target.getAsMention() + " has been unblacklisted").queue();
    }
}
