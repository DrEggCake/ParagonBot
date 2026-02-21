package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Unban implements ICommand {

    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public String getDescription() {
        return "Unban a user from the server with a specified reason.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "user", "User to unban (enter ID)", true),
                new OptionData(OptionType.STRING, "reason", "Reason to unban the user", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        if (executer == null) return;

        String userId = event.getOption("user").getAsString();
        String reason = event.getOption("reason").getAsString();



        if(!(executer.getRoles().contains(Utilities.SR_MOD_ROLE) || executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.SR_MOD_ROLE) || role.getPosition()>= Utilities.SR_MOD_ROLE.getPosition()) )){
            event.reply("No permissions").setEphemeral(true).queue();
        }

        try {
            // Try to retrieve the user from Discord (even if not in the guild)
            User bannedUser = Main.jda.retrieveUserById(userId).complete();
            if (bannedUser == null) {
                event.reply("User not found.").setEphemeral(true).queue();
                return;
            }

            // Check database for the ban
            String checkSql = "SELECT 1 FROM bans WHERE userid=?";
            try (PreparedStatement checkStmt = Main.conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, userId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        event.reply("User is not banned in the database.").setEphemeral(true).queue();
                        return;
                    }
                }
            }

            // Delete from database
            String deleteSql = "DELETE FROM bans WHERE userid=?";
            try (PreparedStatement deleteStmt = Main.conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, userId);
                deleteStmt.executeUpdate();
            }

            // Attempt to unban in Discord
            event.getGuild().unban(bannedUser).queue(
                    success -> System.out.println("Successfully unbanned user: " + userId),
                    error -> System.out.println("Failed to unban user: " + userId)
            );

            // Build log embed
            EmbedBuilder logEmbed = new EmbedBuilder()
                    .setTitle("User Unbanned")
                    .setDescription("User unbanned: " + bannedUser.getAsTag() + " (" + userId + ")")
                    .addField("Staff", executer.getUser().getAsTag(), false)
                    .addField("Reason to unban", reason, false)
                    .setColor(Color.BLUE);

            if (bannedUser.getAvatarUrl() != null) {
                logEmbed.setThumbnail(bannedUser.getAvatarUrl());
            }

            // Reply to command and log in mod channel
            event.replyEmbeds(logEmbed.build()).queue(
                    msg -> {
                        if (Utilities.PUNISHMENT_LOG_CHANNEL_ID != null) Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(logEmbed.build()).queue();
                    }
            );

        } catch (NumberFormatException e) {
            event.reply("Invalid user ID. Please enter a valid numeric ID.").setEphemeral(true).queue();
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("Database error occurred.").setEphemeral(true).queue();
        }
    }
}
