package com.dreggcake.src.Listeners.Logs;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TestLogging extends ListenerAdapter {

    private static final int BULK_THRESHOLD = 5;

    private static final int MAX_CACHE_PER_CHANNEL = 500;
    private static final long BULK_TIMEOUT = 2000;

    private final Map<Long, LinkedHashMap<Long, CachedMessage>> MESSAGE_CACHE = new ConcurrentHashMap<>();
    private final Map<Long, BulkDeleteSession> BULK_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void onReady(ReadyEvent e) {
        System.out.println("[LOGGING] Ready");
    }

    /* ---------------- READY ---------------- */

    @Override
    public void onGuildReady(GuildReadyEvent e) {
        e.getGuild().getTextChannels().forEach(this::preloadMessages);
    }

    private LinkedHashMap<Long, CachedMessage> createCache() {
        return new LinkedHashMap<>(MAX_CACHE_PER_CHANNEL, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Long, CachedMessage> eldest) {
                return size() > MAX_CACHE_PER_CHANNEL;
            }
        };
    }

    /* ---------------- CACHE ---------------- */

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        MESSAGE_CACHE.computeIfAbsent(e.getChannel().getIdLong(), id -> createCache())
                .put(e.getMessageIdLong(), new CachedMessage(e.getMessage()));
    }

    private void preloadMessages(TextChannel channel) {
        MESSAGE_CACHE.putIfAbsent(channel.getIdLong(), createCache());
        channel.getIterableHistory().takeAsync(MAX_CACHE_PER_CHANNEL)
                .thenAccept(msgs -> msgs.forEach(m ->
                        MESSAGE_CACHE.get(channel.getIdLong()).put(m.getIdLong(), new CachedMessage(m))));
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent e) {
        if (Utilities.DO_NOT_DELETE_CHANNEL == null) return;

        var cache = MESSAGE_CACHE.get(e.getChannel().getIdLong());
        if (cache == null) return;

        CachedMessage msg = cache.remove(e.getMessageIdLong());
        if (msg == null || msg.isBot()) return;

        long channelId = e.getChannel().getIdLong();
        BULK_SESSIONS.putIfAbsent(channelId, new BulkDeleteSession());
        BulkDeleteSession session = BULK_SESSIONS.get(channelId);

        session.messages.add(msg);
        session.lastDeleteTime = System.currentTimeMillis();

        // Schedule the purge/bulk check
        CompletableFuture.delayedExecutor(BULK_TIMEOUT, TimeUnit.MILLISECONDS).execute(() -> {
            BulkDeleteSession s = BULK_SESSIONS.get(channelId);
            if (s == null) return;

            if (System.currentTimeMillis() - s.lastDeleteTime >= BULK_TIMEOUT) {
                if (s.messages.size() >= BULK_THRESHOLD) {
                    // Treat as a purge
                    logPurge(s.messages, e.getGuild(), e.getChannel().asTextChannel());
                } else {
                    // Log single deletions
                    for (CachedMessage m : s.messages) {
                        fetchAuditEntry(e.getGuild(), ActionType.MESSAGE_DELETE, String.valueOf(m.getAuthorId()), entry -> {
                            String deleter = entry == null ? "Unknown" : entry.getUser().getAsMention();
                            logSingleDelete(m, e.getGuild(), "<#" + m.getChannelId() + ">", deleter);
                        });
                    }
                }
                BULK_SESSIONS.remove(channelId);
            }
        });
    }

    private void logPurge(List<CachedMessage> messages, Guild guild, TextChannel channel) {
        StringBuilder sb = new StringBuilder();
        for (CachedMessage msg : messages) {
            sb.append("Author: ").append(msg.getAuthorId()).append("\n")
                    .append("Timestamp: ").append(msg.getTimestamp()).append("\n")
                    .append("Content: ").append(msg.getContent()).append("\n");
            msg.getAttachmentUrls().forEach(u -> sb.append("Attachment: ").append(u).append("\n"));
            sb.append("\n-----------------\n");
        }

        try {
            File file = File.createTempFile("purge-", ".txt");
            Files.writeString(file.toPath(), sb.toString());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Messages Purged")
                    .setColor(Color.ORANGE)
                    .setDescription("Messages: **" + messages.size() + "**")
                    .addField("Channel", channel.getAsMention(), false);

            Utilities.DO_NOT_DELETE_CHANNEL
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(file))
                    .queue(s -> file.delete());

        } catch (Exception ignored) {
        }
    }

    // Updated single delete to include deleter
    private void logSingleDelete(CachedMessage msg, Guild guild, String channelMention, String deleter) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Message Deleted")
                .setColor(Color.RED)
                .addField("Author", "<@" + msg.getAuthorId() + ">", true)
                .addField("Deleted By", deleter, true)
                .addField("Channel", channelMention, false);

        String content = msg.getContent();
        if (content.length() > 1024) {
            try {
                File f = File.createTempFile("deleted-", ".txt");
                Files.writeString(f.toPath(), content);
                embed.setDescription("Content too long, see attachment.");
                Utilities.DO_NOT_DELETE_CHANNEL
                        .sendMessageEmbeds(embed.build())
                        .addFiles(FileUpload.fromData(f))
                        .queue(s -> f.delete());
            } catch (Exception ignored) {
            }
        } else {
            embed.addField("Content", content.isBlank() ? "N/A" : "```" + content + "```", false);
            Utilities.DO_NOT_DELETE_CHANNEL.sendMessageEmbeds(embed.build()).queue();
        }
    }


    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent e) {
        if (Utilities.DO_NOT_DELETE_CHANNEL == null) return;

        List<CachedMessage> messages = e.getMessageIds().stream()
                .map(id -> {
                    var cache = MESSAGE_CACHE.get(e.getChannel().getIdLong());
                    if (cache == null) return null;
                    return cache.remove(id);
                })
                .filter(msg -> msg != null && !msg.isBot())
                .toList();

        if (messages.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (CachedMessage msg : messages) {
            sb.append("Author: ")
                    .append(msg.getAuthorId())
                    .append("\n")
                    .append("Timestamp:")
                    .append(msg.getTimestamp())
                    .append("\n")
                    .append("Content:\n")
                    .append(msg.getContent())
                    .append("\n");
            msg.getAttachmentUrls().forEach(u -> sb.append("Attachment: ").append(u).append("\n"));
            sb.append("\n-----------------\n");
        }

        try {
            File file = File.createTempFile("bulk-delete-", ".txt");
            Files.writeString(file.toPath(), sb.toString());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Messages purged")
                    .setColor(Color.ORANGE)
                    .setDescription("Messages: **" + messages.size() + "**")
                    .addField("Channel", e.getChannel().getAsMention(), false);

            Utilities.DO_NOT_DELETE_CHANNEL
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(file))
                    .queue(s -> file.delete());

        } catch (Exception ignored) {
        }
    }




    /* ---------------- BULK DELETE ---------------- */


    private void fetchAuditEntry(Guild guild, ActionType type, String targetId, Consumer<net.dv8tion.jda.api.audit.AuditLogEntry> cb) {
        guild.retrieveAuditLogs().type(type).limit(6).queue(entries -> {
            for (var entry : entries) {
                if (!entry.getTargetId().equals(targetId)) continue;

                if (entry.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(6))) {
                    cb.accept(entry);
                    return;
                }
            }
            cb.accept(null);
        }, err -> cb.accept(null));
    }


    /* ---------------- AUDIT LOG FETCH ---------------- */

    @Override
    public void onGuildBan(GuildBanEvent e) {
        fetchAuditEntry(e.getGuild(), ActionType.BAN, e.getUser().getId(), entry -> {

            User mod = entry == null ? null : entry.getUser();
            String reason = entry == null || entry.getReason() == null ? "N/A" : entry.getReason();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("User Banned")
                    .setColor(Color.RED)
                    .addField("User", e.getUser().getAsMention(), true)
                    .addField("Moderator", mod == null ? "Unknown" : mod.getAsMention(), true)
                    .addField("Reason", reason, false);

            Utilities.DO_NOT_DELETE_CHANNEL.sendMessageEmbeds(embed.build()).queue();
        });
    }


    /* ---------------- BAN / KICK / TIMEOUT ---------------- */

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        fetchAuditEntry(e.getGuild(), ActionType.KICK, e.getUser().getId(), entry -> {
            if (entry == null) return; // user left normally

            User mod = entry.getUser();
            String reason = entry.getReason() == null ? "N/A" : entry.getReason();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("User Kicked")
                    .setColor(Color.RED)
                    .addField("User", e.getUser().getAsMention(), true)
                    .addField("Moderator", mod.getAsMention(), true)
                    .addField("Reason", reason, false);

            Utilities.DO_NOT_DELETE_CHANNEL.sendMessageEmbeds(embed.build()).queue();
        });
    }


    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent e) {
        fetchAuditEntry(e.getGuild(), ActionType.MEMBER_UPDATE, e.getUser().getId(), entry -> {

            User mod = entry == null ? null : entry.getUser();
            String reason = entry == null || entry.getReason() == null ? "N/A" : entry.getReason();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("User Timed Out")
                    .setColor(Color.ORANGE)
                    .addField("User", e.getUser().getAsMention(), true)
                    .addField("Moderator", mod == null ? "Unknown" : mod.getAsMention(), true)
                    .addField("Reason", reason, false);

            Utilities.DO_NOT_DELETE_CHANNEL.sendMessageEmbeds(embed.build()).queue();
        });
    }


    private FileUpload downloadAttachment(String url, String name) {
        try (InputStream in = new URL(url).openStream()) {
            return FileUpload.fromData(in.readAllBytes(), name);
        } catch (Exception e) {
            return null;
        }
    }

    /* ---------------- ATTACHMENT SMART DOWNLOAD ---------------- */

    @Override
    public void onUserUpdateName(UserUpdateNameEvent e) {
        logNameChange("Global Name Changed", e.getUser(), e.getOldName(), e.getNewName());
    }

    /* ---------------- PROFILE / NICK / GLOBAL NAME ---------------- */

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
        logNameChange("Nickname Updated", e.getUser(), e.getOldNickname(), e.getNewNickname());
    }

    private void logNameChange(String title, User user, String oldN, String newN) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .addField("Old", String.valueOf(oldN), true)
                .addField("New", String.valueOf(newN), true)
                .setFooter(user.getId(), user.getEffectiveAvatarUrl());


        Utilities.DO_NOT_DELETE_CHANNEL.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {
        if (e.getChannelJoined() != null)
            voiceLog(e.getMember(), "Joined Voice Channel", "#" + e.getChannelJoined().getName(), Color.GREEN);
        if (e.getChannelLeft() != null)
            voiceLog(e.getMember(), "Left Voice Channel", "#" + e.getChannelLeft().getName(), Color.RED);
    }

    /* ---------------- VOICE ---------------- */

    private void voiceLog(Member m, String action, String ch, Color c) {
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(m.getUser().getName(), null, m.getEffectiveAvatarUrl())
                .setTitle(action)
                .setColor(c)
                .setDescription(m.getAsMention() + " → " + ch);

        Utilities.DO_NOT_DELETE_CHANNEL.sendMessageEmbeds(embed.build()).queue();
    }

    private static class BulkDeleteSession {
        final List<CachedMessage> messages = new CopyOnWriteArrayList<>();
        volatile long lastDeleteTime = System.currentTimeMillis();
    }

}
