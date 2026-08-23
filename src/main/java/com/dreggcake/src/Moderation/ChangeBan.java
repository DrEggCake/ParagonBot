package com.dreggcake.src.Moderation;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import com.dreggcake.src.Utility.Channels;

public class ChangeBan implements ICommand {

    @Override
    public String getName() {
        return "change-ban";
    }

    @Override
    public String getDescription() {
        return "Change the ban time of a user";
    }

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

        // Permission check
        if(executer.getRoles().stream().noneMatch(ModUtility.getChangeBanStaff()::contains)){
            event.reply("No permissions.").setEphemeral(true).queue();
            return;
        }

        String bannedUserId = event.getOption("userid").getAsString();

        String durationStr = event.getOption("new-duration").getAsString();
        long newDurationMillis = System.currentTimeMillis() + parseTimeToMillis(durationStr);
        String changeReason = event.getOption("reason").getAsString();

        try {
            // Fetch old duration
            BanData data = Databases.getMainDatabase().query(
                    "SELECT duration FROM bans WHERE userid = ?",
                    rs -> {
                        if (rs.next()) {
                            return new BanData(rs.getString("duration"));
                        }
                        return null;
                    },
                    bannedUserId
            );

            if (data == null) {
                event.reply("User is not banned in the database.")
                        .setEphemeral(true).queue();
                return;
            }

            long oldDurationMillis = Long.parseLong(data.duration);

            // Update DB
            Databases.getMainDatabase().update(
                    "UPDATE bans SET duration = ?, changed_ban_reason = ?, old_duration = ? WHERE userid = ?",
                    String.valueOf(newDurationMillis),
                    changeReason,
                    data.duration,
                    bannedUserId
            );

            // Fetch user + respond
            event.getJDA().retrieveUserById(bannedUserId).queue(
                    user -> {
                        EmbedBuilder embed = generateBanEmbed(
                                event, executer, user,
                                oldDurationMillis, newDurationMillis, changeReason
                        );

                        event.replyEmbeds(embed.build()).queue();

                        Channels.PUNISHMENT_LOG
                                .sendMessageEmbeds(embed.build())
                                .queue();
                    },
                    failure -> {
                        EmbedBuilder embed = generateBanEmbed(
                                event, executer, null,
                                oldDurationMillis, newDurationMillis, changeReason
                        );

                        event.replyEmbeds(embed.build()).queue();

                        Channels.PUNISHMENT_LOG
                                .sendMessageEmbeds(embed.build())
                                .queue();
                    }
            );

        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("DAO error occurred.").setEphemeral(true).queue();
        }
    }

    private EmbedBuilder generateBanEmbed(SlashCommandInteractionEvent event,
                                          Member executer,
                                          User bannedUser,
                                          long oldMillis,
                                          long newMillis,
                                          String reason) {

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Ban Updated")
                .setColor(Color.ORANGE)
                .setFooter("Executed by: " + executer.getUser().getAsTag(),
                        executer.getUser().getEffectiveAvatarUrl());

        if (bannedUser != null) {
            embed.setThumbnail(bannedUser.getEffectiveAvatarUrl());
        }

        embed.addField("Banned User",
                bannedUser != null
                        ? bannedUser.getAsMention()
                        : "ID: " + event.getOption("userid").getAsString(),
                true);

        embed.addField("Reason for Alteration", reason, true);
        embed.addField("Old Ban Duration", formatTimestamp(oldMillis), true);
        embed.addField("New Ban Duration", formatTimestamp(newMillis), true);
        embed.addField("Time Remaining", formatTimeRemaining(newMillis), false);

        return embed;
    }

    private long parseTimeToMillis(String str) {
        long time = 0L;

        if (str.endsWith("h")) {
            time = Long.parseLong(str.replace("h", "")) * 3600_000L;
        } else if (str.endsWith("d")) {
            time = Long.parseLong(str.replace("d", "")) * 86_400_000L;
        } else if (str.endsWith("m")) {
            time = Long.parseLong(str.replace("m", "")) * 60_000L;
        } else if (str.endsWith("s")) {
            time = Long.parseLong(str.replace("s", "")) * 1000L;
        }

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

        return String.format("%d days, %d hours, %d minutes, %d seconds",
                days, hours, minutes, seconds);
    }

    // Helper class
    private static class BanData {
        String duration;

        public BanData(String duration) {
            this.duration = duration;
        }
    }
}