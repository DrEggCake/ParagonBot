package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.Database.DAO;
import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.ICommand;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mute implements ICommand {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public String getName() {
        return "mute-set";
    }

    @Override
    public String getDescription() {
        return "Mute a user from the server for a specified time.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "User to mute", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the mute (h, d, m, s)", true),
                new OptionData(OptionType.STRING, "reason", "Reason for mute", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        User muted = event.getOption("user").getAsUser();
        String duration = event.getOption("duration").getAsString();

        long delay = parseTimeToSeconds(duration);

        if (delay <= 0) {
            event.reply("Duration cannot be zero or negative!").setEphemeral(true).queue();
            return;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long epochTime = (currentTime + delay) * 1000;

        String reason = (event.getOption("reason") == null)
                ? "N/A"
                : event.getOption("reason").getAsString();

        // Permission check
        if (!(executer.getRoles().contains(Utilities.SUPPORT_ROLE)
                || executer.getRoles().stream().anyMatch(role ->
                role.equals(Utilities.SUPPORT_ROLE)
                        || role.getPosition() >= Utilities.SUPPORT_ROLE.getPosition()))) {

            event.reply("No permissions").setEphemeral(true).queue();
            return;
        }

        try {
            // Check if already muted
            boolean alreadyMuted = Databases.getMainDatabase().query(
                    "SELECT 1 FROM mutes WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    muted.getId()
            );

            if (alreadyMuted) {
                event.reply("User already muted. You can only timeout")
                        .setEphemeral(true).queue();
                return;
            }

            // Role hierarchy check
            Member targetMember = event.getGuild().getMember(muted);
            if (targetMember != null) {
                Role mutedHighest = targetMember.getRoles().isEmpty()
                        ? null
                        : targetMember.getRoles().get(0);

                if (mutedHighest != null
                        && mutedHighest.getPosition() >= Utilities.SUPPORT_ROLE.getPosition()) {

                    event.reply("I can't mute that user, check my roles and permissions.")
                            .setEphemeral(true).queue();
                    return;
                }
            }

            // Apply mute role
            event.getGuild().addRoleToMember(muted, Utilities.MUTED_ROLE).queue(
                    success -> {},
                    failure -> event.getChannel()
                            .sendMessage("Error muting: " + failure.getMessage())
                            .queue(msg -> msg.delete().queueAfter(3, TimeUnit.SECONDS))
            );

            // Build embed
            EmbedBuilder replyEmbed = new EmbedBuilder();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant unMuteTime = timestamp.toInstant().plusSeconds(delay);

            replyEmbed.setTitle("User Muted");
            replyEmbed.setDescription("A user was muted in the server");
            replyEmbed.addField("User Muted:", muted.getAsTag(), false);
            replyEmbed.addField("Muted User Id:", muted.getId(), false);
            replyEmbed.addField("Staff who muted:", executer.getUser().getAsTag(), false);
            replyEmbed.addField("Reason to mute:", reason, false);
            replyEmbed.addField("Mute Duration:", duration, false);
            replyEmbed.addField("Timestamp:",
                    TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
            replyEmbed.addField("Unmute Time:",
                    TimeFormat.DATE_TIME_LONG.format(unMuteTime), false);
            replyEmbed.setThumbnail(muted.getEffectiveAvatarUrl());
            replyEmbed.setColor(Color.RED);

            event.replyEmbeds(replyEmbed.build()).queue(msg ->
                    Utilities.PUNISHMENT_LOG_CHANNEL_ID
                            .sendMessageEmbeds(replyEmbed.build()).queue()
            );

            // Insert into DB
            Databases.getMainDatabase().update(
                    "INSERT INTO mutes (userid, duration, reason, staffid) VALUES (?, ?, ?, ?)",
                    muted.getId(),
                    String.valueOf(epochTime),
                    reason,
                    executer.getId()
            );

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
        } else if (time.endsWith("m")) {
            duration = Long.parseLong(time.replace("m", "")) * 60L;
        }

        return duration;
    }
}