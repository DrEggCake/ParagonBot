package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
        return List.of(new OptionData(OptionType.USER, "user", "User to remove from blacklist", true),
                new OptionData(OptionType.STRING, "reason", "Reason to remove from blacklist", true));

    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        if (executer == null) return;

        Guild guild = event.getGuild();
        if (guild == null) return;

        User user = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();

        if (Utilities.SR_MOD_ROLE == null || Utilities.TICKET_BLACKLIST_ROLE == null || Utilities.PUNISHMENT_LOG_CHANNEL_ID == null) {
            event.reply("Roles or mod-log channel are misconfigured").setEphemeral(true).queue();
            return;
        }

        // Permission check
        boolean hasPermission = executer.getRoles().stream()
                .anyMatch(role -> role.equals(Utilities.SR_MOD_ROLE) || role.getPosition() >= Utilities.SR_MOD_ROLE.getPosition());

        if (!hasPermission) {
            event.reply("You don't have permissions").setEphemeral(true).queue();
            return;
        }

        // Database check
        String selectSql = "SELECT reason FROM blacklistTicket WHERE userid = ?";
        String deleteSql = "DELETE FROM blacklistTicket WHERE userid = ?";

        try (PreparedStatement selectStmt = Main.conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, user.getId());
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    event.reply("User is not blacklisted").setEphemeral(true).queue();
                    return;
                }

                String blacklistReason = rs.getString("reason");

                // Delete from database
                try (PreparedStatement deleteStmt = Main.conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, user.getId());
                    deleteStmt.executeUpdate();
                }

                // Remove role from user and then reply
                guild.removeRoleFromMember(user, Utilities.TICKET_BLACKLIST_ROLE).queue(
                        success -> {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle("Ticket Blacklist Edited");
                            embed.setDescription("User was removed from ticket blacklist");
                            embed.addField("User:", user.getAsMention(), false);
                            embed.addField("Staff:", executer.getAsMention(), false);
                            embed.addField("Reason to remove from blacklist:", reason, false);
                            embed.addField("Original blacklist reason:", blacklistReason, false);
                            embed.setThumbnail(user.getEffectiveAvatarUrl());
                            embed.setColor(Color.CYAN);

                            // 1️⃣ Send to mod-log channel
                            Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(embed.build()).queue();

                            // 2️⃣ Optional: also reply to command
                            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        },
                        error -> event.reply("Failed to remove role from user").setEphemeral(true).queue()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("Database error occurred").setEphemeral(true).queue();
        }
    }


}
