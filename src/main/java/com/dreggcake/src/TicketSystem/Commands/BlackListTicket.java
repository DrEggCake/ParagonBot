package com.dreggcake.src.TicketSystem.Commands;

import com.dreggcake.src.Moderation.ModUtility;
import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.Roles;

public class BlackListTicket implements ICommand {

    @Override
    public String getName() {
        return "blacklist-ticket";
    }

    @Override
    public String getDescription() {
        return "Blacklist a user from opening a ticket.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "User to blacklist", true),
                new OptionData(OptionType.STRING, "reason", "Reason to blacklist", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the blacklist", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        User blacklisted = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();
        String time = event.getOption("duration").getAsString();

        // Permission check
        if(executer.getRoles().stream().noneMatch(ModUtility.getBlacklistTicketStaff()::contains)){
            event.reply("You do not have permission.").setEphemeral(true).queue();
            return;
        }


        try {
            // Check if already blacklisted
            boolean alreadyBlacklisted = Databases.getMainDatabase().query(
                    "SELECT 1 FROM blacklistTicket WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    blacklisted.getId()
            );

            if (alreadyBlacklisted) {

                // Fetch existing data
                BlacklistData data = Databases.getMainDatabase().query(
                        "SELECT staffid, duration, reason FROM blacklistTicket WHERE userid = ?",
                        rs -> {
                            if (rs.next()) {
                                return new BlacklistData(
                                        rs.getString("staffid"),
                                        rs.getString("duration"),
                                        rs.getString("reason")
                                );
                            }
                            return null;
                        },
                        blacklisted.getId()
                );

                if (data != null) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("User is already blacklisted");
                    embed.setThumbnail(blacklisted.getEffectiveAvatarUrl());
                    embed.setDescription(blacklisted.getAsTag());

                    Timestamp timestamp = new Timestamp(Long.parseLong(data.duration));
                    Instant unblacklistTime = timestamp.toInstant();

                    Member staffMember = event.getGuild().getMemberById(data.staffId);

                    embed.addField("Staff:",
                            staffMember != null ? staffMember.getAsMention() : data.staffId,
                            false);

                    embed.addField("Reason:", data.reason, false);
                    embed.addField("Expires:",
                            TimeFormat.DATE_TIME_LONG.format(unblacklistTime),
                            false);

                    embed.setColor(Color.BLACK);

                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                }

                return;
            }

            // Calculate time
            long delay = parseTimeToSeconds(time);
            long currentTime = System.currentTimeMillis() / 1000;
            long epochTime = (currentTime + delay) * 1000;

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant unblacklistTime = timestamp.toInstant().plusSeconds(delay);

            // Insert into DB
            Databases.getMainDatabase().update(
                    "INSERT INTO blacklistTicket(userid, reason, duration, staffid) VALUES (?, ?, ?, ?)",
                    blacklisted.getId(),
                    reason,
                    String.valueOf(epochTime),
                    executer.getId()
            );

            // Build embed
            EmbedBuilder replyEmbed = new EmbedBuilder();
            replyEmbed.setTitle("User ticket Blacklisted");
            replyEmbed.setDescription("A user was blacklisted from tickets in the server");
            replyEmbed.addField("User:", blacklisted.getAsMention(), false);
            replyEmbed.addField("User ID:", blacklisted.getId(), false);
            replyEmbed.addField("Staff:", executer.getAsMention(), false);
            replyEmbed.addField("Reason:", reason, false);
            replyEmbed.addField("Timestamp:",
                    TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
            replyEmbed.addField("Expires:",
                    TimeFormat.DATE_TIME_LONG.format(unblacklistTime), false);
            replyEmbed.setThumbnail(blacklisted.getEffectiveAvatarUrl());

            // Apply role + send message
            event.getGuild().addRoleToMember(blacklisted, Roles.TICKET_BLACKLIST)
                    .queue(success -> event.replyEmbeds(replyEmbed.build()).queue(msg ->
                            Channels.PUNISHMENT_LOG
                                    .sendMessageEmbeds(replyEmbed.build()).queue()
                    ));

        } catch (SQLException e) {
            e.printStackTrace();
            event.reply("DAO error occurred.").setEphemeral(true).queue();
        }
    }

    private long parseTimeToSeconds(String time) {
        long duration = 0L;

        if (time.endsWith("h")) {
            duration = Long.parseLong(time.replace("h", "")) * 3600L;
        } else if (time.endsWith("d")) {
            duration = Long.parseLong(time.replace("d", "")) * 86400L;
        } else if (time.endsWith("s")) {
            duration = Long.parseLong(time.replace("s", ""));
        }

        return duration;
    }

    // Helper class for query result
    private static class BlacklistData {
        String staffId;
        String duration;
        String reason;

        public BlacklistData(String staffId, String duration, String reason) {
            this.staffId = staffId;
            this.duration = duration;
            this.reason = reason;
        }
    }
}