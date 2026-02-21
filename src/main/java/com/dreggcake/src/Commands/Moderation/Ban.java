package com.dreggcake.src.Commands.Moderation;

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

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Ban
        implements ICommand {
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
        return List.of(new OptionData(OptionType.USER, "user", "User to ban", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the ban eg: 1h (h for hour, d for days, m for minutes) ", true),
                new OptionData(OptionType.STRING, "reason", "Reason for ban", true),
                new OptionData(OptionType.STRING, "delete_messages", "Delete messages of this hour of past time ( h for hour, d for days, m for minutes", false));

    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
/*        if (!event.getChannelId().equals("1251202767893037056")) {
            event.reply("Not the right channel baby! Do it in <#1251202767893037056>").setEphemeral(true).queue();
            return;
        }*/
        User banned = null;
        String bannedId;
        String userIdOrMention = event.getOption("user").getAsString();
        try {
            if (event.getOption("user").getAsUser() != null) {
                banned = event.getOption("user").getAsUser();
                bannedId = banned.getId();
            } else {
                bannedId = userIdOrMention;
                banned = event.getJDA().retrieveUserById(event.getOption("user").getAsString()).complete();

            }
        } catch (Exception e) {
//            event.reply("User not found. Please provide a valid mention or user ID.").setEphemeral(true).queue();
            bannedId = userIdOrMention;
//            return;
        }
        String duration = event.getOption("duration").getAsString();
        String deletionmsg;
        if (event.getOption("messagesdelete") == null) {
            deletionmsg = "0";
        } else {
            deletionmsg = event.getOption("messagesdelete").getAsString();
        }
        long deletionFrame = this.parseTimeToSeconds(deletionmsg);

        long delay = this.parseTimeToSeconds(duration);
        long currentTime = System.currentTimeMillis() / 1000;
        long addtime = currentTime + delay;
        long epochTime = addtime * 1000;

        Role modRole = event.getGuild().getRoleById("1404312044890624160");
        String reason;
        if (event.getOption("reason") == null) {
            reason = "N/A";
        } else {
            reason = event.getOption("reason").getAsString();
        }

        if(!(executer.getRoles().contains(Utilities.JR_MOD_ROLE) || executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.JR_MOD_ROLE) || role.getPosition()>= Utilities.JR_MOD_ROLE.getPosition()) )){
            event.reply("No permissions").setEphemeral(true).queue();
        } else {
            try {
                String check = "SELECT userid,duration,reason  FROM bans";
                Statement stmt = Main.conn.createStatement();
                ResultSet rs = stmt.executeQuery(check);
                List<String> names = new ArrayList<>();
                List<String> reasons = new ArrayList<>();
                List<String> times = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString("userid");
                    String oldReason = rs.getString("reason");
                    String time = rs.getString("duration");
                    names.add(name);
                    reasons.add(oldReason);
                    times.add(time);
                }
                if (names.contains(banned.getId())) {
                    event.reply("User already banned. To extend ban, use /change-ban").setEphemeral(true).queue();
                    return;
                } else {
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
                        replyEmbed.addField("Banned User ID: ", bannedId, false);
                    }
                    replyEmbed.addField("Staff who banned:", executer.getUser().getAsTag(), false);
                    replyEmbed.addField("Reason for ban:", reason, false);
                    replyEmbed.addField("Ban Duration:", duration, false);
                    replyEmbed.addField("Timestamp: ", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
                    replyEmbed.addField("Unban Time: ", TimeFormat.DATE_TIME_LONG.format(unbanTime), false);
                    replyEmbed.setThumbnail(banned.getEffectiveAvatarUrl());
                    Member selfUser = event.getGuild().getSelfMember();
                    Member banny = event.getGuild().getMemberById(bannedId);

                    if (banny == null) {
                        event.getGuild().ban(Main.jda.retrieveUserById(bannedId).complete(), (int) deletionFrame, TimeUnit.SECONDS).queue();
                        event.replyEmbeds(replyEmbed.build()).queue((msg) -> {
                            Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(replyEmbed.build()).queue();
                        });
                    } else {
                        Role selfHighestRole = selfUser.getRoles().stream().findFirst().orElse(null);

                        Role bannyHighestRole = banny.getRoles().stream().findFirst().orElse(null);
                        if (selfHighestRole == null || bannyHighestRole == null || selfHighestRole.getPosition() <= bannyHighestRole.getPosition()) {
                            event.reply("I can't ban this user. They have a higher role!").setEphemeral(true).queue();
                            return;
                        }
                        event.getGuild().ban(Main.jda.retrieveUserById(bannedId).complete(), (int) deletionFrame, TimeUnit.SECONDS).queue();
                        event.replyEmbeds(replyEmbed.build()).queue((msg) -> {
                            Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(replyEmbed.build()).queue();
                        });
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            String sql = "INSERT INTO bans (userid,duration,reason,staffid) VALUES (?, ?, ?, ?)";
            try {
                PreparedStatement pstmt = Main.conn.prepareStatement(sql);
                pstmt.setString(1, bannedId);
                pstmt.setString(2, String.valueOf(epochTime));
                pstmt.setString(3, reason);
                pstmt.setString(4, executer.getId());
                pstmt.execute();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    private long parseTimeToSeconds(String time) {
        long duration = 0L;

        if (time.endsWith("h")) {
            duration = Long.parseLong(time.replace("h", "")) * 3600L; // hours to seconds
        } else if (time.endsWith("d")) {
            duration = Long.parseLong(time.replace("d", "")) * 86400L; // days to seconds
        } else if (time.endsWith("s")) {
            duration = Long.parseLong(time.replace("s", "")); // seconds
        } else if (time.endsWith("m")) {
            duration = Long.parseLong(time.replace("m", "")) * 60L; // minutes to seconds
        }

        return duration;
    }

}

