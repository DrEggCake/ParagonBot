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
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
        return List.of(new OptionData(OptionType.USER, "user", "User to blacklist", true),
                new OptionData(OptionType.STRING, "reason", "Reason to blacklist", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the blacklist", true));

    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();

        User blacklisted = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();
        String time = event.getOption("duration").getAsString();
        if (!executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.SR_MOD_ROLE) || role.getPosition() >= Utilities.SR_MOD_ROLE.getPosition())) {
            event.reply("You don't have permission").setEphemeral(true).queue();
            return;
        }
/*        if (!event.getChannel().asTextChannel().equals(cmdChannel)) {
            event.reply("Use this command in " + cmdChannel.getAsMention()).setEphemeral(true).queue();
        }*/
        try {
            String select = "SELECT * FROM blacklistTicket";
            PreparedStatement pstmt = Main.conn.prepareStatement(select);
            ResultSet rs = pstmt.executeQuery();
            List<String> userids = new ArrayList<>();
            List<String> durations = new ArrayList<>();
            List<String> reasons = new ArrayList<>();
            List<String> staffids = new ArrayList<>();

            while (rs.next()) {
                String userid = rs.getString("userid");
                String duration = rs.getString("duration");
                String reason1 = rs.getString("reason");
                String staffid = rs.getString("staffid");
                userids.add(userid);
                durations.add(duration);
                reasons.add(reason1);
                staffids.add(staffid);
            }
            if (userids.contains(blacklisted.getId())) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("User is already blacklisted");
                embed.setThumbnail(blacklisted.getEffectiveAvatarUrl());
                embed.setDescription(blacklisted.getAsTag());
                String getStaff = "SELECT * FROM blacklistTicket WHERE userid=?";
                PreparedStatement pstmt1 = Main.conn.prepareStatement(getStaff);
                pstmt1.setString(1, blacklisted.getId());
                ResultSet gg = pstmt1.executeQuery();
                String getstaffid = gg.getString("staffid");
                String getduration = gg.getString("duration");
                String getReason = gg.getString("reason");
                Timestamp timestamp = new Timestamp(Long.parseLong(getduration));
                Instant unblacklistTime = timestamp.toInstant();
                embed.addField("Staff: ", event.getGuild().getMemberById(getstaffid).getAsMention(), false);
                embed.addField("Reason to blacklist: ", getReason, false);
                embed.addField("Duration: ", TimeFormat.DATE_TIME_LONG.format(unblacklistTime), false);
                embed.setColor(Color.BLACK);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            }


            try {
                String insert = "INSERT INTO blacklistTicket(userid,reason,duration,staffid) VALUES(?, ?, ?, ?)";
                PreparedStatement pstmt2 = Main.conn.prepareStatement(insert);
                pstmt2.setString(1, blacklisted.getId());
                pstmt2.setString(2, reason);
                long delay = this.parseTimeToSeconds(time);
                long currentTime = System.currentTimeMillis() / 1000;
                long addTime = currentTime + delay;
                long epochTime = addTime * 1000;
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                Instant unblacklistTime = timestamp.toInstant().plusSeconds(delay);
                pstmt2.setString(3, String.valueOf(epochTime));
                pstmt2.setString(4, executer.getId());
                pstmt2.execute();


                EmbedBuilder replyEmbed = new EmbedBuilder();
                replyEmbed.setTitle("User ticket Blacklisted");
                replyEmbed.setDescription("A user was blacklisted from tickets in the server");
                replyEmbed.addField("User blacklisted: ", blacklisted.getAsMention(), false);
                replyEmbed.addField("Blacklisted user id: ", blacklisted.getId(), false);
                replyEmbed.addField("Staff: ", executer.getAsMention(), false);
                replyEmbed.addField("Reason to blacklist: ", reason, false);
                replyEmbed.addField("Timestamp: ", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
                replyEmbed.addField("Expires on: ", TimeFormat.DATE_TIME_LONG.format(unblacklistTime), false);
                replyEmbed.setThumbnail(blacklisted.getEffectiveAvatarUrl());

                event.getGuild().addRoleToMember(blacklisted, Utilities.TICKET_BLACKLIST_ROLE).queue(success -> {
                    event.replyEmbeds(replyEmbed.build()).queue((msg) -> {
                        Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(replyEmbed.build()).queue();
                    });
                });


            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
}
