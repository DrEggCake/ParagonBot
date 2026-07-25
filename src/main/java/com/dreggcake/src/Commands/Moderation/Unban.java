package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.Database.DAO;
import com.dreggcake.src.Database.Databases;
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

        // Permission check
        if(executer.getRoles().stream().noneMatch(ModUtility.getUnbanModStaff()::contains)){
            event.reply("No permissions.").setEphemeral(true).queue();
            return;
        }

        try {
            // Retrieve user
            User bannedUser = Main.jda.retrieveUserById(userId).complete();

            if (bannedUser == null) {
                event.reply("User not found.").setEphemeral(true).queue();
                return;
            }

            // Check if user is banned in DB
            boolean isBanned = Databases.getMainDatabase().query(
                    "SELECT 1 FROM bans WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    userId
            );

            if (!isBanned) {
                event.reply("User is not banned in the database.")
                        .setEphemeral(true).queue();
                return;
            }

            // Delete from DB
            Databases.getMainDatabase().update(
                    "DELETE FROM bans WHERE userid = ?",
                    userId
            );

            // Unban from Discord
            event.getGuild().unban(bannedUser).queue(
                    success -> System.out.println("Successfully unbanned user: " + userId),
                    error -> System.out.println("Failed to unban user: " + userId)
            );

            // Build embed
            EmbedBuilder logEmbed = new EmbedBuilder()
                    .setTitle("User Unbanned")
                    .setDescription("User unbanned: " + bannedUser.getAsTag() + " (" + userId + ")")
                    .addField("Staff", executer.getUser().getAsTag(), false)
                    .addField("Reason to unban", reason, false)
                    .setColor(Color.BLUE);

            if (bannedUser.getAvatarUrl() != null) {
                logEmbed.setThumbnail(bannedUser.getAvatarUrl());
            }

            // Reply + log
            event.replyEmbeds(logEmbed.build()).queue(msg -> {
                if (Utilities.PUNISHMENT_LOG_CHANNEL_ID != null) {
                    Utilities.PUNISHMENT_LOG_CHANNEL_ID
                            .sendMessageEmbeds(logEmbed.build())
                            .queue();
                }
            });

        } catch (NumberFormatException e) {
            event.reply("Invalid user ID. Please enter a valid numeric ID.")
                    .setEphemeral(true).queue();

        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("DAO error occurred.").setEphemeral(true).queue();
        }
    }
}