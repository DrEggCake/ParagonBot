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

public class ChangeBan implements ICommand {

    @Override
    public String getName() { return "change-ban"; }

    @Override
    public String getDescription() { return "Change the ban time of a user"; }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "userid", "User ID to edit ban", true),
                new OptionData(OptionType.STRING, "reason", "Reason for altering ban", true),
                new OptionData(OptionType.STRING, "new-duration", "New ban duration (e.g., 1d, 2h)", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        if (executer == null) return;
        if(!(executer.getRoles().contains(Utilities.SR_MOD_ROLE) || executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.JR_MOD_ROLE) || role.getPosition()>= Utilities.JR_MOD_ROLE.getPosition()) )){
            event.reply("No permissions").setEphemeral(true).queue();
            return;
        }

        String bannedUserId = event.getOption("userid").getAsString();
        String durationStr = event.getOption("new-duration").getAsString();
        long newDurationMillis = System.currentTimeMillis() + parseTimeToMillis(durationStr);
        String changeReason = event.getOption("reason").getAsString();

        // Fetch the banned user from DB
        String selectSql = "SELECT * FROM bans WHERE userid = ?";
        String updateSql = "UPDATE bans SET duration = ?, changed_ban_reason = ?, old_duration = ? WHERE userid = ?";

        try (PreparedStatement selectStmt = Main.conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, bannedUserId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    event.reply("User is not banned in the database.").setEphemeral(true).queue();
                    return;
                }

                String oldDurationStr = rs.getString("duration");
                long oldDurationMillis = Long.parseLong(oldDurationStr);

                // Update DB
                try (PreparedStatement updateStmt = Main.conn.prepareStatement(updateSql)) {
                    updateStmt.setLong(1, newDurationMillis);
                    updateStmt.setString(2, changeReason);
                    updateStmt.setString(3, oldDurationStr);
                    updateStmt.setString(4, bannedUserId);
                    updateStmt.executeUpdate();
                }

                // Retrieve user info for thumbnail safely
                event.getJDA().retrieveUserById(bannedUserId).queue(
                        user -> sendBanEmbed(event, executer, user, oldDurationMillis, newDurationMillis, changeReason),
                        failure -> sendBanEmbed(event, executer, null, oldDurationMillis, newDurationMillis, changeReason)
                );

            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("Database error occurred.").setEphemeral(true).queue();
        }
    }

    private void sendBanEmbed(SlashCommandInteractionEvent event, Member executer, User bannedUser, long oldMillis, long newMillis, String reason) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Ban Updated")
                .setColor(Color.ORANGE)
                .setFooter("Executed by: " + executer.getUser().getAsTag(), executer.getUser().getEffectiveAvatarUrl());

        if (bannedUser != null) embed.setThumbnail(bannedUser.getEffectiveAvatarUrl());

        embed.addField("Banned User", bannedUser != null ? bannedUser.getAsMention() : "ID: " + event.getOption("userid").getAsString(), true);
        embed.addField("Reason for Alteration", reason, true);
        embed.addField("Old Ban Duration", formatTimestamp(oldMillis), true);
        embed.addField("New Ban Duration", formatTimestamp(newMillis), true);
        embed.addField("Time Remaining", formatTimeRemaining(newMillis), false);

        event.replyEmbeds(embed.build()).queue();
    }

    private long parseTimeToMillis(String str) {
        long time = 0L;
        if (str.endsWith("h")) time = Long.parseLong(str.replace("h", "")) * 3600_000L;
        else if (str.endsWith("d")) time = Long.parseLong(str.replace("d", "")) * 86_400_000L;
        else if (str.endsWith("m")) time = Long.parseLong(str.replace("m", "")) * 60_000L;
        else if (str.endsWith("s")) time = Long.parseLong(str.replace("s", "")) * 1000L;
        return time;
    }

    private String formatTimestamp(long millis) {
        return "<t:" + (millis / 1000) + ":F>";
    }

    private String formatTimeRemaining(long millis) {
        long remaining = millis - System.currentTimeMillis();
        if (remaining <= 0) return "Ban has expired";
        long seconds = remaining / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
    }
}
