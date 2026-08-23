package com.dreggcake.src.AutoModService.CapsService;

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
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.dreggcake.src.Utility.Channels;

public class CapsListener extends ListenerAdapter {

    private static final int MIN_LENGTH = 8;
    private static final double CAPS_RATIO = 0.7;

    private static final long WARNING_EXPIRY = 10_000; // 10 seconds

    // userId -> last warning timestamp
    private final Map<Long, Long> warningCache = new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember() == null) return;

        Member member = event.getMember();
        Message message = event.getMessage();
        Member bot = event.getGuild().getSelfMember();

        if (!isCapsSpam(message.getContentRaw())) return;
        if (AutoModBypassService.shouldBypass(member)) return;
        if (AutoModChannelBypassService.shouldBypass(event.getChannel())) return;
        if (AutoModCategoryBypassService.shouldCategoryBypass(event.getMessage().getCategory())) return;

        long now = System.currentTimeMillis();
        long userId = member.getIdLong();

        Long lastWarning = warningCache.get(userId);

        // 🔹 First offense → warn
        if (lastWarning == null || now - lastWarning > WARNING_EXPIRY) {
            warningCache.put(userId, now);
            message.delete().queue(
                    s -> {
                    },
                    f -> {
                    }
            );
            sendWarning(event.getChannel().asTextChannel(), member);
            return;
        }

        // 🔹 Second offense within 10s → timeout
        if (!canTimeout(bot, member)) return;

        warningCache.remove(userId);
        handleCaps(event, member);
    }

    // ================= CAPS DETECTION =================

    private boolean isCapsSpam(String content) {
        if (content.length() < MIN_LENGTH)
            return false;

        int letters = 0;
        int caps = 0;

        for (char c : content.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) {
                    caps++;
                }
            }
        }

        if (letters == 0)
            return false;

        return ((double) caps / letters) >= CAPS_RATIO;
    }

    // ================= ACTIONS =================

    private void handleCaps(MessageReceivedEvent event, Member member) {
        TextChannel channel = event.getChannel().asTextChannel();
        long userId = member.getIdLong();

        deleteRecentMessages(channel, userId);
        timeoutMember(member);
        sendTimeoutEmbed(channel, member);
    }

    private void deleteRecentMessages(TextChannel channel, long userId) {
        channel.getHistory().retrievePast(100).queue(messages -> {
            long now = System.currentTimeMillis();

            messages.stream()
                    .filter(msg -> msg.getAuthor().getIdLong() == userId)
                    .filter(msg -> now - msg.getTimeCreated().toInstant().toEpochMilli() <= 5_000)
                    .forEach(msg -> msg.delete().queue(
                            s -> {
                            },
                            f -> {
                            }
                    ));
        });
    }

    private void timeoutMember(Member member) {
        member.timeoutFor(Duration.ofMinutes(2))
                .reason("AutoMod: Excessive Caps")
                .queue();
    }

    // ================= EMBEDS =================

    private void sendWarning(TextChannel channel, Member member) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setAuthor(member.getUser().getName() + " has been warned.", null, member.getEffectiveAvatarUrl())
                .addField("Reason:", "Excessive use of caps", true)
                .addField("Support:", "Think this is wrong? Let us know at " + Channels.SUPPORT.getAsMention(), false)
                .setFooter("ParagonMC Auto Moderation")
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private void sendTimeoutEmbed(TextChannel channel, Member member) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(40, 133, 225))
                .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                .setDescription(member.getAsMention() + " has been **Timed Out**")
                .addField("Reason", "Excessive use of capital letters", false)
                .addField("Duration", "2 minutes", false)
                .setFooter("ParagonMC Auto Moderation")
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    // ================= PERMISSIONS =================

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
