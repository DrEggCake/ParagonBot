package com.dreggcake.src.AutoModService.LinkService;

import com.dreggcake.src.AutoModService.Common.AutoModBypassService;
import com.dreggcake.src.AutoModService.Common.AutoModCategoryBypassService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Duration;
import java.util.Set;
import java.util.regex.Pattern;

public class LinkListener extends ListenerAdapter {
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)\\b(" +
                    "(https?://|www\\.)\\S+" +            // https www
                    "|" +
                    "discord\\.gg/\\S+" +                 // dc invite
                    "|" +
                    "([a-z0-9-]+\\.)+[a-z]{2,}" +          // domain tld
                    "|" +
                    "\\b(\\d{1,3}\\.){3}\\d{1,3}\\b"       // IP
                    + ")"
    );
    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "youtube.com",
            "youtu.be",
            "imgur.com",
            "gyazo.com",
            "tenor.com",
            "klipy.com",
            "giphy.com",
            "cdn.discordapp.com",
            "paragonnetwork.us",
            "google.com",
            "duckduckgo.com",
            "amazon.com",
            "amazon.in",
            "paragon.com",
            "paragonmc.com",
            "paragon.net",
            "paragonmc.net",
            "paragonnetwork.com",
            "paragonnetwork.net"
    );

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember() == null) return;

        Message message = event.getMessage();
        Member member = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        if (!isLink(message)) return;
        if (AutoModBypassService.shouldBypass(member)) return;
        if (AutoModCategoryBypassService.shouldCategoryBypass(event.getMessage().getCategory())) return;
        if (!shouldModerateLinks(event.getChannel())) return;
        if (isAllowedLink(message.getContentRaw())) return;

        handleLink(member, message, channel);
    }

    private void handleLink(Member member, Message message, TextChannel channel) {
        message.delete().queue();
        sendMessage(channel, member);

    }

    private boolean isLink(Message message) {
        String content = message.getContentRaw();

        if (content == null || content.isBlank())
            return false;

        return LINK_PATTERN.matcher(content).find();
    }

    private void sendMessage(TextChannel channel, Member member) {

        String message = ":warning: " + member.getAsMention() + " Links are not allowed! :warning:";

        channel.sendMessage(message)
                .delay(Duration.ofSeconds(3))
                .flatMap(Message::delete)
                .queue();
    }

    private boolean shouldModerateLinks(MessageChannelUnion channel) {

        // Ignore DMs
        if (!channel.getType().isGuild())
            return false;

        // Allow forum posts
        if (channel.getType() == ChannelType.FORUM)
            return false;

        // Allow forum threads
        if (channel.getType() == ChannelType.GUILD_PUBLIC_THREAD)
            return false;

        // Block links everywhere else (text + voice text)
        return channel.getType() == ChannelType.TEXT
                || channel.getType() == ChannelType.VOICE;
    }

    private boolean isAllowedLink(String content) {
        String lower = content.toLowerCase();

        for (String domain : ALLOWED_DOMAINS) {
            if (lower.contains(domain)) {
                return true;
            }
        }
        return false;
    }


}
