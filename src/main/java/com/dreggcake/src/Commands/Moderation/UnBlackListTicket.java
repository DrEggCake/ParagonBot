package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.Database.DAO;
import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.ICommand;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UnBlackListTicket implements ICommand {

    @Override
    public String getName() {
        return "remove-blacklist-ticket";
    }

    @Override
    public String getDescription() {
        return "Removes a user from ticket blacklist";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "User to remove from blacklist", true),
                new OptionData(OptionType.STRING, "reason", "Reason to remove from blacklist", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        if (executer == null) return;

        Guild guild = event.getGuild();
        if (guild == null) return;

        User user = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();

        if (Utilities.SR_MOD_ROLE == null
                || Utilities.TICKET_BLACKLIST_ROLE == null
                || Utilities.PUNISHMENT_LOG_CHANNEL_ID == null) {

            event.reply("Roles or mod-log channel are misconfigured")
                    .setEphemeral(true).queue();
            return;
        }

        // Permission check
        boolean hasPermission = executer.getRoles().stream()
                .anyMatch(role -> role.equals(Utilities.SR_MOD_ROLE)
                        || role.getPosition() >= Utilities.SR_MOD_ROLE.getPosition());

        if (!hasPermission) {
            event.reply("You don't have permissions").setEphemeral(true).queue();
            return;
        }

        try {
            // Fetch blacklist reason
            String blacklistReason = Databases.getMainDatabase().query(
                    "SELECT reason FROM blacklistTicket WHERE userid = ?",
                    rs -> {
                        if (rs.next()) {
                            return rs.getString("reason");
                        }
                        return null;
                    },
                    user.getId()
            );

            if (blacklistReason == null) {
                event.reply("User is not blacklisted")
                        .setEphemeral(true).queue();
                return;
            }

            // Delete from DB
            Databases.getMainDatabase().update(
                    "DELETE FROM blacklistTicket WHERE userid = ?",
                    user.getId()
            );

            // Remove role
            guild.removeRoleFromMember(user, Utilities.TICKET_BLACKLIST_ROLE).queue(
                    success -> {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Ticket Blacklist Edited");
                        embed.setDescription("User was removed from ticket blacklist");
                        embed.addField("User:", user.getAsMention(), false);
                        embed.addField("Staff:", executer.getAsMention(), false);
                        embed.addField("Reason to remove:", reason, false);
                        embed.addField("Original blacklist reason:", blacklistReason, false);
                        embed.setThumbnail(user.getEffectiveAvatarUrl());
                        embed.setColor(Color.CYAN);

                        // Log + reply
                        Utilities.PUNISHMENT_LOG_CHANNEL_ID
                                .sendMessageEmbeds(embed.build())
                                .queue();

                        event.replyEmbeds(embed.build())
                                .setEphemeral(true)
                                .queue();
                    },
                    error -> event.reply("Failed to remove role from user")
                            .setEphemeral(true).queue()
            );

        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("DAO error occurred").setEphemeral(true).queue();
        }
    }
}