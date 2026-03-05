package com.dreggcake.src.Commands.Moderation;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Unmute
        implements ICommand {
    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public String getDescription() {
        return "Removes the mute of the user";
    }

    @Override
    public List<OptionData> getOptions() {
        ArrayList<OptionData> urmom = new ArrayList<OptionData>();
        urmom.add(new OptionData(OptionType.USER, "user", "The user you wish to unmute", true));
        urmom.add(new OptionData(OptionType.STRING, "reason", "Reason for unmuting", true));
        return urmom;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member executer = event.getMember();
/*        if (!event.getChannelId().equals("1251202767893037056")) {
            event.reply("Not the right channel baby! Do it in <#1251202767893037056>").setEphemeral(true).queue();
            return;
        }*/


        if (!executer.getRoles().stream().anyMatch(role -> role.equals(Utilities.SR_MOD_ROLE) || role.getPosition() > Utilities.SR_MOD_ROLE.getPosition())) {
            event.reply("You don't have necessary permissions.").setEphemeral(true).queue();
            return;
        }
        try {
            User muted = event.getJDA().getUserById(event.getOption("user").getAsString());
            if (muted == null) {
                event.reply("The entered User does not exist.").setEphemeral(true).queue();
                return;
            }
            String unmute = muted.getId();
            String check = "SELECT userid, staffid, reason FROM mutes WHERE userid=?";
            String sql = "DELETE FROM mutes WHERE userid= " + unmute;
            try {
                PreparedStatement pstmt = Main.conn.prepareStatement(check);
                pstmt.setString(1, unmute);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String staffid = rs.getString("staffid");
                    Member staff = event.getGuild().retrieveMember(UserSnowflake.fromId(staffid)).complete();
                    String unmutedID = rs.getString("userid");
                    String reason = rs.getString("reason");
                    Statement stmt = Main.conn.createStatement();
                    event.getGuild().removeRoleFromMember(muted, Utilities.MUTED_ROLE).queue();
                    EmbedBuilder replyEmbed = new EmbedBuilder();
                    replyEmbed.setTitle("Unmute");
                    replyEmbed.setDescription("A user was unmuted.");
                    replyEmbed.addField("Unmuted user ID", unmutedID, false);
                    replyEmbed.addField("Staff ", staff.getUser().getAsTag(), false);
                    replyEmbed.addField("Reason for unmute", event.getOption("reason").getAsString(), false);
                    replyEmbed.addField("Reason of muting", reason, false);
                    replyEmbed.setThumbnail(event.getGuild().getMemberById(unmutedID).getEffectiveAvatarUrl());
                    event.replyEmbeds(replyEmbed.build()).queue((msg) -> {
                        Utilities.PUNISHMENT_LOG_CHANNEL_ID.sendMessageEmbeds(replyEmbed.build()).queue();
                    });
                    stmt.executeUpdate(sql);
                    stmt.close();
                } else {
                    event.reply("User is not muted or does not exist").setEphemeral(true).queue();
                    return;
                }
                pstmt.close();
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (NumberFormatException e) {
            event.reply("Invalid user id, Did you enter a string? :0").setEphemeral(true).queue();
        }
    }


}
