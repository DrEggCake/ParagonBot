package com.dreggcake.src.Media.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Media.Logic.MediaDAO;
import com.dreggcake.src.Media.MediaPermissions;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;

public class MediaRankCommand implements ICommand {
    @Override
    public String getName() {
        return "media-rank";
    }

    @Override
    public String getDescription() {
        return "No description";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "action", "Action to take", true)
                        .addChoice("Add", "add")
                        .addChoice("Remove", "remove"),
                new OptionData(OptionType.USER, "user", "The targeted user", true),
                new OptionData(OptionType.STRING, "duration", "The duration", true),
                new OptionData(OptionType.STRING, "role", "Selected role", true)
                        .addChoice("Content Creator I", "creator1")
                        .addChoice("Content Creator II", "creator2")
                        .addChoice("Content Creator III", "creator3")
                        .addChoice("Content Creator IV", "creator4")
                        .addChoice("Partner", "partner")
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            return;
        }

        Member executer = event.getMember();
        if (executer == null || !MediaPermissions.canManagePermission(executer)) {
            event.reply("No permissions").setEphemeral(true).queue();
            return;
        }
        String roleType = event.getOption("role").getAsString();

        Role role;

        switch (roleType) {
            case "creator1":
                role = Utilities.CONTENT_CREATOR_1_ROLE;
                break;
            case "creator2":
                role = Utilities.CONTENT_CREATOR_2_ROLE;
                break;
            case "creator3":
                role = Utilities.CONTENT_CREATOR_3_ROLE;
                break;
            case "creator4":
                role = Utilities.CONTENT_CREATOR_4_ROLE;
                break;
            case "partner":
                role = Utilities.PARTNER_ROLE;
                break;
            default:
                event.reply("Invalid role selection.")
                        .setEphemeral(true)
                        .queue();
                return;
        }

        String action = event.getOption("action").getAsString();

        if (action.equals("add")) {
            handleAdd(event, role);
        } else {
            handleRemove(event, role);
        }


    }

    private void handleRemove(SlashCommandInteractionEvent event, Role role) {
        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.reply("Invalid user").setEphemeral(true).queue();
            return;
        }

        if (MediaDAO.isMember(target)) {
            MediaDAO.removeMemberRank(target);
        }
        if (target.getRoles().contains(role)) {
            event.getGuild().removeRoleFromMember(target, role).queue();
        }

        event.reply("Member removed from Databass").queue();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Media Rank Removed");
        embed.addField("Staff", event.getMember().getAsMention(), true);
        embed.addField("Target", target.getAsMention(), true);
        embed.setColor(Color.RED);
        Utilities.MEDIA_LOG_CHANNEL.sendMessageEmbeds(embed.build()).queue();

    }

    private void handleAdd(SlashCommandInteractionEvent event, Role role) {
        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.reply("Invalid user").setEphemeral(true).queue();
            return;
        }

        String dur = event.getOption("duration").getAsString();
        long delay = parseTimeToSeconds(dur);

        long currentTime = System.currentTimeMillis() / 1000;
        long epochTime = (currentTime + delay) * 1000;

        MediaDAO.addMemberRank(target, epochTime, event.getMember());
        event.getGuild().addRoleToMember(target, role).queue();

        event.reply("Added " + target.getAsMention() + " to media rank database").queue();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Media Rank Added");
        embed.addField("Staff", event.getMember().getAsMention(), true);
        embed.addField("Target", target.getAsMention(), true);
        embed.setColor(Color.BLUE);
        Utilities.MEDIA_LOG_CHANNEL.sendMessageEmbeds(embed.build()).queue();
    }

    private long parseTimeToSeconds(String time) {
        long duration = 0L;

        if (time.endsWith("h")) {
            duration = Long.parseLong(time.replace("h", "")) * 3600L;
        } else if (time.endsWith("d")) {
            duration = Long.parseLong(time.replace("d", "")) * 86400L;
        } else if (time.endsWith("s")) {
            duration = Long.parseLong(time.replace("s", ""));
        } else if (time.endsWith("m")) {
            duration = Long.parseLong(time.replace("m", "")) * 60L;
        }

        return duration;
    }

}
