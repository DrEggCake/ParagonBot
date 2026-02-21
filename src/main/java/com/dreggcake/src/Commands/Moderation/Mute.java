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

import java.awt.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mute
        implements ICommand {
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
        return List.of(new OptionData(OptionType.USER, "user", "User to mute", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the mute ( h for hours, d for days, s for seconds", true),
                new OptionData(OptionType.STRING, "reason", "Reason for mute", true));

    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
/*        if (!event.getChannelId().equals("1251202767893037056")) {
            event.reply("Not the right channel baby! Do it in <#1251202767893037056>").setEphemeral(true).queue();
            return;
        }*/
        User muted = event.getOption("user").getAsUser();
        String duration = event.getOption("duration").getAsString();

        long delay = this.parseTimeToSeconds(duration);
        if(delay<=0){
            event.reply("Duration cannot be zero or negative!").setEphemeral(true).queue();
        }
        long currentTime = System.currentTimeMillis() / 1000;
        long addtime = currentTime + delay;
        long epochTime = addtime * 1000;


        String reason;
        if (event.getOption("reason") == null) {
            reason = "N/A";
        } else {
            reason = event.getOption("reason").getAsString();
        }

        if(!(executer.getRoles().contains(Utilities.SUPPORT_ROLE) || executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.SUPPORT_ROLE) || role.getPosition()>=Utilities.SUPPORT_ROLE.getPosition()) )){
            event.reply("No permissions").setEphemeral(true).queue();
        } else {
            try {
                String check = "SELECT userid,duration,reason FROM mutes";
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
                if (names.contains(muted.getId())) {
                    event.reply("User already muted. You can only timeout").setEphemeral(true).queue();
                    return;
                } else {
                    Role botHighest = event.getGuild().getSelfMember().getRoles().isEmpty() ? null : event.getGuild().getSelfMember().getRoles().get(0);
                    Role mutedHighest = event.getGuild().getMember(muted).getRoles().isEmpty() ? null : event.getGuild().getMember(muted).getRoles().get(0);

                    if (mutedHighest.getPosition() >= Utilities.SUPPORT_ROLE.getPosition()) {
                        event.reply("I can't mute that user, check my roles and permissions.").setEphemeral(true).queue();
                        return;
                    }

                    event.getGuild().addRoleToMember(muted, Utilities.MUTED_ROLE).queue(
                            success -> {
                            },
                            failure -> event.getChannel().sendMessage("Error muting." + failure.getMessage()).queue(msg -> msg.delete().queueAfter(3, TimeUnit.SECONDS))
                    );
                    EmbedBuilder replyEmbed = new EmbedBuilder();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Instant unMuteTime = timestamp.toInstant().plusSeconds(delay);

                    replyEmbed.setTitle("User Muted");
                    replyEmbed.setDescription("A user was muted in the server");
                    replyEmbed.addField("User Muted:", muted.getAsTag(), false);
                    replyEmbed.addField("Muted User Id:", muted.getId(), false);
                    replyEmbed.addField("Staff who muted:", executer.getUser().getAsTag(), false);
                    replyEmbed.addField("Reason to mute: ", reason, false);
                    replyEmbed.addField("Mute Duration:", duration, false);
                    replyEmbed.addField("Timestamp:", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
                    replyEmbed.addField("Unmute Time:", TimeFormat.DATE_TIME_LONG.format(unMuteTime), false);
                    replyEmbed.setThumbnail(muted.getEffectiveAvatarUrl());
                    replyEmbed.setColor(Color.RED);
                    event.replyEmbeds(replyEmbed.build()).queue((msg) -> {
                        Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(replyEmbed.build()).queue();
                    });

                }
                stmt.close();
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            String sql = "INSERT INTO mutes (userid,duration,reason,staffid) VALUES (?, ?, ?, ?)";
            try {
                PreparedStatement pstmt = Main.conn.prepareStatement(sql);
                pstmt.setString(1, muted.getId());
                pstmt.setString(2, String.valueOf(epochTime));
                pstmt.setString(3, reason);
                pstmt.setString(4, executer.getId());
                pstmt.execute();
                pstmt.close();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
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
