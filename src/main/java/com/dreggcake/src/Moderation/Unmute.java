package com.dreggcake.src.Moderation;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.Roles;
import com.dreggcake.src.Utility.PermissionUtility;

public class Unmute implements ICommand {

    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public String getDescription() {
        return "Removes the mute of the user";
    }

    @Override
    public List<OptionData> getOptions() {
        ArrayList<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "The user you wish to unmute", true));
        options.add(new OptionData(OptionType.STRING, "reason", "Reason for unmuting", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        if (executer == null) return;

        // Permission check
        if(executer.getRoles().stream().noneMatch(PermissionUtility.getUnmuteModStaff()::contains)){
            event.reply("No permissions.").setEphemeral(true).queue();
            return;
        }


        try {
            User muted = event.getOption("user").getAsUser();
            if (muted == null) {
                event.reply("The entered user does not exist.")
                        .setEphemeral(true).queue();
                return;
            }

            String userId = muted.getId();

            // Fetch mute data
            MuteData data = Databases.getMainDatabase().query(
                    "SELECT userid, staffid, reason FROM mutes WHERE userid = ?",
                    rs -> {
                        if (rs.next()) {
                            return new MuteData(
                                    rs.getString("userid"),
                                    rs.getString("staffid"),
                                    rs.getString("reason")
                            );
                        }
                        return null;
                    },
                    userId
            );

            if (data == null) {
                event.reply("User is not muted or does not exist")
                        .setEphemeral(true).queue();
                return;
            }

            // Remove role
            event.getGuild().removeRoleFromMember(muted, Roles.MUTED).queue();

            // Delete from DB
            Databases.getMainDatabase().update(
                    "DELETE FROM mutes WHERE userid = ?",
                    userId
            );

            // Get staff member
            Member staff = event.getGuild()
                    .retrieveMember(UserSnowflake.fromId(data.staffId))
                    .complete();

            // Build embed
            EmbedBuilder replyEmbed = new EmbedBuilder();
            replyEmbed.setTitle("Unmute");
            replyEmbed.setDescription("A user was unmuted.");
            replyEmbed.addField("Unmuted user ID", data.userId, false);
            replyEmbed.addField("Staff",
                    staff != null ? staff.getUser().getAsTag() : data.staffId,
                    false);
            replyEmbed.addField("Reason for unmute",
                    event.getOption("reason").getAsString(), false);
            replyEmbed.addField("Reason of muting", data.reason, false);

            Member targetMember = event.getGuild().getMemberById(data.userId);
            if (targetMember != null) {
                replyEmbed.setThumbnail(targetMember.getEffectiveAvatarUrl());
            }

            // Reply + log
            event.replyEmbeds(replyEmbed.build()).queue(msg ->
                    Channels.PUNISHMENT_LOG
                            .sendMessageEmbeds(replyEmbed.build()).queue()
            );

        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("DAO error occurred.")
                    .setEphemeral(true).queue();

        } catch (NumberFormatException e) {
            event.reply("Invalid user id.")
                    .setEphemeral(true).queue();
        }
    }

    // Helper class
    private static class MuteData {
        String userId;
        String staffId;
        String reason;

        public MuteData(String userId, String staffId, String reason) {
            this.userId = userId;
            this.staffId = staffId;
            this.reason = reason;
        }
    }
}