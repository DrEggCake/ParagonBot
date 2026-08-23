package com.dreggcake.src.Logging;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Logging extends ListenerAdapter {

    private static final int MAX_CACHE_PER_CHANNEL = 500;

    /**
     * Member ID -> Roles
     */
    private static final Map<String, List<Role>> MEMBER_ROLES = new ConcurrentHashMap<>();

    /**
     * Channel ID -> (Message ID -> Message)
     */
    private final Map<Long, Map<Long, Message>> MESSAGE_CACHE = new ConcurrentHashMap<>();

    /* ---------------- READY ---------------- */

    public static void cacheMemberRoles(Member member) {
        if (member == null) return;
        MEMBER_ROLES.put(member.getId(), member.getRoles());
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot ready in " + event.getJDA().getGuilds().size() + " guilds.");
    }

    /* ---------------- MESSAGE CACHE ---------------- */

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Guild guild = event.getGuild();

        for (Member member : guild.getMembers()) {
            MEMBER_ROLES.put(member.getId(), member.getRoles());
        }

        for (TextChannel channel : guild.getTextChannels()) {
            MESSAGE_CACHE.putIfAbsent(channel.getIdLong(), createCache());
            preloadMessages(channel, MESSAGE_CACHE.get(channel.getIdLong()));
        }

        System.out.println("[LOGGING] Cached roles & messages for " + guild.getName());
    }

    /* ---------------- MESSAGE DELETE ---------------- */

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        MESSAGE_CACHE
                .computeIfAbsent(event.getChannel().getIdLong(), id -> createCache())
                .put(event.getMessageIdLong(), event.getMessage());
    }


    /* ---------------- MESSAGE EDIT ---------------- */

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (Utilities.MESSAGES_LOG_CHANNEL == null) return;

        Map<Long, Message> cache = MESSAGE_CACHE.get(event.getChannel().getIdLong());
        if (cache == null) return;

        Message msg = cache.remove(event.getMessageIdLong());
        if (msg == null || msg.getAuthor().isBot()) return;

        String content = msg.getContentRaw();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Message Deleted")
                .setDescription("Channel: " + msg.getChannel().getAsMention())
                .addField("Author", msg.getAuthor().getAsMention(), false)
                .setFooter("User ID: " + msg.getAuthor().getId(),
                        msg.getAuthor().getEffectiveAvatarUrl());

        // 🔒 1024 char protection
        if (content.length() > 1024) {
            attachDeleteFile(embed, content);
        } else {
            embed.addField(
                    "Content",
                    content.isBlank() ? "N/A" : "```" + content + "```",
                    false
            );
            sendWithAttachments(embed, msg.getAttachments());
        }
    }

    /* ---------------- ROLES ---------------- */

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (Utilities.MESSAGES_LOG_CHANNEL == null) return;
        if (event.getMessage().getAuthor().isBot()) return;

        Map<Long, Message> cache = MESSAGE_CACHE
                .computeIfAbsent(event.getChannel().getIdLong(), id -> createCache());

        Message oldMsg = cache.get(event.getMessageIdLong());
        String oldContent = oldMsg == null ? "" : oldMsg.getContentRaw();
        String newContent = event.getMessage().getContentRaw();

        if (oldContent.equals(newContent)) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Message Edited", event.getMessage().getJumpUrl())
                .addField("Author", event.getMember().getAsMention(), false)
                .setFooter("Message ID: " + event.getMessageId());

        if (oldContent.length() > 1024 || newContent.length() > 1024) {
            attachDiffFile(embed, oldContent, newContent);
        } else {
            embed.addField("Old", oldContent.isBlank() ? "N/A" : oldContent, false);
            embed.addField("New", newContent.isBlank() ? "N/A" : newContent, false);
        }

        sendWithAttachments(embed, event.getMessage().getAttachments());
        cache.put(event.getMessageIdLong(), event.getMessage());
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        logRoles(event.getMember(), event.getRoles(), "Roles Added", Color.YELLOW);
    }

    /* ---------------- PROFILE UPDATES ---------------- */

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        List<Role> before = MEMBER_ROLES.getOrDefault(event.getMember().getId(), List.of());
        List<Role> removed = before.stream()
                .filter(r -> !event.getMember().getRoles().contains(r))
                .toList();

        logRoles(event.getMember(), removed, "Roles Removed", Color.RED);
        MEMBER_ROLES.put(event.getMember().getId(), event.getMember().getRoles());
    }

    @Override
    public void onGuildMemberUpdateAvatar(GuildMemberUpdateAvatarEvent event) {
        logAvatar("Server Avatar Updated", event.getMember().getUser());
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        logAvatar("Global Avatar Updated", event.getUser());
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Global Name Updated")
                .addField("Old", event.getOldGlobalName() == null ? "None" : event.getOldGlobalName(), true)
                .addField("New", event.getNewGlobalName() == null ? "None" : event.getNewGlobalName(), true)
                .setFooter(event.getUser().getId(), event.getUser().getEffectiveAvatarUrl());

        Utilities.MEMBER_LOG_CHANNEL_ID.sendMessageEmbeds(embed.build()).queue();
    }

    /* ---------------- HELPERS ---------------- */

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Nickname Updated")
                .addField("Old", event.getOldNickname() == null ? "None" : event.getOldNickname(), true)
                .addField("New", event.getNewNickname() == null ? "None" : event.getNewNickname(), true)
                .setFooter(event.getUser().getId(), event.getUser().getEffectiveAvatarUrl());

        Utilities.MEMBER_LOG_CHANNEL_ID.sendMessageEmbeds(embed.build()).queue();
    }

    private void preloadMessages(TextChannel channel, Map<Long, Message> cache) {
        channel.getIterableHistory()
                .takeAsync(MAX_CACHE_PER_CHANNEL)
                .thenAccept(messages -> messages.forEach(m -> cache.put(m.getIdLong(), m)));
    }

    private Map<Long, Message> createCache() {
        return Collections.synchronizedMap(new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Message> eldest) {
                return size() > MAX_CACHE_PER_CHANNEL;
            }
        });
    }

    private void sendWithAttachments(EmbedBuilder embed, List<Message.Attachment> attachments) {
        List<CompletableFuture<FileUpload>> futures = new ArrayList<>();

        for (Message.Attachment att : attachments) {
            futures.add(att.getProxy().download()
                    .thenApply(data -> FileUpload.fromData(data, att.getFileName())));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> Utilities.MESSAGES_LOG_CHANNEL
                        .sendMessageEmbeds(embed.build())
                        .addFiles(futures.stream().map(CompletableFuture::join).toList())
                        .queue());
    }

    private void attachDiffFile(EmbedBuilder embed, String oldC, String newC) {
        try {
            File f = File.createTempFile("edit-", ".txt");
            Files.writeString(f.toPath(), "OLD:\n" + oldC + "\n\nNEW:\n" + newC);
            embed.setDescription("Content too long, see attachment.");
            Utilities.MESSAGES_LOG_CHANNEL
                    .sendFiles(FileUpload.fromData(f))
                    .queue();
        } catch (IOException ignored) {
        }
    }

    private void attachDeleteFile(EmbedBuilder embed, String content) {
        try {
            File f = File.createTempFile("deleted-", ".txt");
            Files.writeString(f.toPath(), content);

            embed.setDescription("Content too long, see attachment.");

            Utilities.MESSAGES_LOG_CHANNEL
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(f))
                    .queue();
        } catch (IOException ignored) {
        }
    }

    private void logRoles(Member member, List<Role> roles, String title, Color color) {
        if (Utilities.MEMBER_LOG_CHANNEL_ID == null || roles.isEmpty()) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setDescription(member.getAsMention());

        roles.forEach(r -> embed.appendDescription("\n• " + r.getAsMention()));
        Utilities.MEMBER_LOG_CHANNEL_ID.sendMessageEmbeds(embed.build()).queue();
    }

    private void logAvatar(String title, User user) {
        if (Utilities.MEMBER_LOG_CHANNEL_ID == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setImage(user.getEffectiveAvatarUrl())
                .setFooter(user.getId(), user.getEffectiveAvatarUrl());

        Utilities.MEMBER_LOG_CHANNEL_ID.sendMessageEmbeds(embed.build()).queue();
    }


}
