package com.dreggcake.src.AutoModService.SpamService;

import com.dreggcake.src.AutoModService.Common.AutoModBypassService;
import com.dreggcake.src.AutoModService.Common.AutoModCategoryBypassService;
import com.dreggcake.src.AutoModService.Common.AutoModChannelBypassService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class SpamListener extends ListenerAdapter {

    private final SpamDetector spamDetector = new SpamDetector();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember() == null) return;
        Member bot = event.getGuild().getSelfMember();
        Message message = event.getMessage();
        Member member = event.getMember();

        if (!spamDetector.isSpam(member.getIdLong(), message.getContentRaw())) return;
        if (AutoModBypassService.shouldBypass(member)) return;
        if (AutoModChannelBypassService.shouldBypass(event.getChannel())) return;
        if (AutoModCategoryBypassService.shouldCategoryBypass(event.getMessage().getCategory())) return;
        if (!canTimeout(bot, member)) return;

        handleSpam(event, member);

    }

    private void handleSpam(MessageReceivedEvent event, Member member) {
        TextChannel channel = event.getChannel().asTextChannel();
        long userId = member.getIdLong();

        deleteSpam(channel, userId); // deletes messages in last 20 seconds

        memberTimeout(member); // Timeout member for 2 minutes

        sendEmbed(channel, member); // Send message Embed

    }

    private void sendEmbed(TextChannel channel, Member member) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(40, 133, 225))
                .setAuthor(member.getUser().getName()+" has been Timed Out", null, member.getEffectiveAvatarUrl())
                .addField("Reason:", "Spamming", false)
                .addField("Duration:", "2 minutes", false)
                .setFooter("ParagonMC Auto Moderation")
                .setTimestamp(java.time.Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private void memberTimeout(Member member) {
        member.timeoutFor(java.time.Duration.ofMinutes(2))
                .reason("AutoMod: Spamming")
                .queue();
    }

    private void deleteSpam(TextChannel channel, long userId) {
        channel.getHistory().retrievePast(100).queue(messages -> {
            long now = System.currentTimeMillis();

            messages.stream()
                    .filter(msg -> msg.getAuthor().getIdLong() == userId)
                    .filter(msg -> now - msg.getTimeCreated().toInstant().toEpochMilli() <= 20_000)
                    .forEach(msg -> msg.delete().queue(
                            success -> {},
                            error -> {} // reduces Unknown message errors
                    ));
        });
    }

    private boolean canTimeout(Member bot, Member target) {
        if (!bot.hasPermission(Permission.MODERATE_MEMBERS))
            return false;

        if (!bot.canInteract(target))
            return false;

        if (target.isOwner())
            return false;

        return true;
    }

}


