package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.Database.DAO;
import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.ICommand;
import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Ban implements ICommand {

    @Override
    public String getName() {
        return "ban-set";
    }

    @Override
    public String getDescription() {
        return "Ban a user from the server for a specified time and reason.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "User to ban", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the ban eg: 1h (h for hour, d for days, m for minutes)", true),
                new OptionData(OptionType.STRING, "reason", "Reason for ban", true),
                new OptionData(OptionType.STRING, "delete_messages", "Delete messages time (e.g. 1h, 1d, 30m)", false)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
        User banned = null;
        String bannedId;

        String userIdOrMention = event.getOption("user").getAsString();

        try {
            if (event.getOption("user").getAsUser() != null) {
                banned = event.getOption("user").getAsUser();
                bannedId = banned.getId();
            } else {
                bannedId = userIdOrMention;
                banned = event.getJDA().retrieveUserById(bannedId).complete();
            }
        } catch (Exception e) {
            bannedId = userIdOrMention;
        }

        String duration = event.getOption("duration").getAsString();

        String deletionmsg = (event.getOption("delete_messages") == null)
                ? "0"
                : event.getOption("delete_messages").getAsString();

        long deletionFrame = parseTimeToSeconds(deletionmsg);
        long delay = parseTimeToSeconds(duration);

        long currentTime = System.currentTimeMillis() / 1000;
        long epochTime = (currentTime + delay) * 1000;

        String reason = (event.getOption("reason") == null)
                ? "N/A"
                : event.getOption("reason").getAsString();

        // Permission check
        if(executer.getRoles().stream().noneMatch(ModUtility.getBanModStaff()::contains)){
            event.reply("You do not have permission.").setEphemeral(true).queue();
            return;
        }


        try {
            // Check if already banned
            boolean alreadyBanned = Databases.getMainDatabase().query(
                    "SELECT 1 FROM bans WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    bannedId
            );

            if (alreadyBanned) {
                event.reply("User already banned. To extend ban, use /change-ban")
                        .setEphemeral(true).queue();
                return;
            }

            // Build embed
            EmbedBuilder replyEmbed = new EmbedBuilder();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Instant unbanTime = timestamp.toInstant().plusSeconds(delay);

            replyEmbed.setTitle("User Banned");
            replyEmbed.setDescription("A user was banned from the server");

            if (banned != null) {
                replyEmbed.addField("User Banned:", banned.getAsTag(), false);
                replyEmbed.addField("Banned User ID:", banned.getId(), false);
                replyEmbed.setThumbnail(banned.getEffectiveAvatarUrl());
            } else {
                replyEmbed.addField("Banned User ID:", bannedId, false);
            }

            replyEmbed.addField("Staff who banned:", executer.getUser().getAsTag(), false);
            replyEmbed.addField("Reason for ban:", reason, false);
            replyEmbed.addField("Ban Duration:", duration, false);
            replyEmbed.addField("Timestamp:", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
            replyEmbed.addField("Unban Time:", TimeFormat.DATE_TIME_LONG.format(unbanTime), false);

            Member selfUser = event.getGuild().getSelfMember();
            Member banny = event.getGuild().getMemberById(bannedId);

            if (banny == null) {
                event.getGuild().ban(Main.jda.retrieveUserById(bannedId).complete(),
                        (int) deletionFrame, TimeUnit.SECONDS).queue();

            } else {
                Role selfHighestRole = selfUser.getRoles().stream().findFirst().orElse(null);
                Role bannyHighestRole = banny.getRoles().stream().findFirst().orElse(null);

                if (selfHighestRole == null || bannyHighestRole == null
                        || selfHighestRole.getPosition() <= bannyHighestRole.getPosition()) {

                    event.reply("I can't ban this user. They have a higher role!")
                            .setEphemeral(true).queue();
                    return;
                }

                event.getGuild().ban(Main.jda.retrieveUserById(bannedId).complete(),
                        (int) deletionFrame, TimeUnit.SECONDS).queue();
            }

            event.replyEmbeds(replyEmbed.build()).queue(msg ->
                    Utilities.PUNISHMENT_LOG_CHANNEL_ID
                            .sendMessageEmbeds(replyEmbed.build()).queue()
            );

            // Insert into database
            Databases.getMainDatabase().update(
                    "INSERT INTO bans (userid, duration, reason, staffid) VALUES (?, ?, ?, ?)",
                    bannedId,
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