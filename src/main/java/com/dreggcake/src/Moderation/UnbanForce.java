package com.dreggcake.src.Moderation.OtherModeration;

import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;

import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.GuildContext;
import com.dreggcake.src.Utility.Roles;

public class UnbanForce implements ICommand {
    @Override
    public String getName() {
        return "unban-force";
    }

    @Override
    public String getDescription() {
        return "Force an unban on a user, even if not in the database";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(
                        OptionType.STRING,
                        "userid",
                        "The ID of the targeted user",
                        true
                ),
                new OptionData(
                        OptionType.STRING,
                        "reason",
                        "Reason to unban",
                        true
                )
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (!hasPermission(member)) {
            event.reply("Nah I refuse").setEphemeral(true).queue();
            return;
        };

        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("Something went wrong? Run this in the server").setEphemeral(true).queue();
            return;
        }

        String userID = event.getOption("userid").getAsString();
        String reason = event.getOption("reason").getAsString();

        guild.unban(UserSnowflake.fromId(userID)).queue(
                success -> {
                    EmbedBuilder embed = successEmbed(member, userID, reason);

                    event.replyEmbeds(embed.build()).queue();

                    // Log
                    Channels.PUNISHMENT_LOG.sendMessageEmbeds(embed.build()).queue();
                }
                ,
                error -> event.reply("❌ Failed to unban user. They may not be banned or the ID is invalid.").setEphemeral(true).queue()
        );


    }

    private EmbedBuilder successEmbed(Member member, String userId, String reason) {
        User bannedUser = GuildContext.JDA.getUserById(userId);
        String userTag = (bannedUser != null)
                ? bannedUser.getAsTag()
                : "Unknown User";

        EmbedBuilder logEmbed = new EmbedBuilder()
                .setTitle("User Unbanned")
                .setDescription("User unbanned: " + userTag + " (" + userId + ")")
                .addField("Staff", member.getUser().getAsTag(), false)
                .addField("Reason to unban", reason, false)
                .setColor(Color.BLUE);

        return logEmbed;
    }


    private boolean hasPermission(Member executer) {
        if (executer.getRoles().isEmpty() && !executer.isOwner()) {
            return false;
        }
        if (!(executer.getRoles().contains(Roles.SR_MOD) || executer.getRoles().stream().anyMatch(role -> role.equals(Roles.SR_MOD) || role.getPosition() >= Roles.SR_MOD.getPosition()))) {
            return false;
        } else {
            return true;
        }
    }
}
