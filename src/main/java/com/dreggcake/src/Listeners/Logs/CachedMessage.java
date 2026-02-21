package com.dreggcake.src.Listeners.Logs;

import com.dreggcake.src.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.Timestamp;

import java.time.OffsetDateTime;
import java.util.List;

public class CachedMessage {

    private final long messageId;
    private final long authorId;
    private final String content;
    private final int embedCount;
    private final List<String> attachmentUrls;
    private final List<String> attachmentNames;
    private final long channelId;
    private OffsetDateTime timestamp;

    public CachedMessage(Message msg) {
        this.messageId = msg.getIdLong();
        this.authorId = msg.getAuthor().getIdLong();
        this.content = msg.getContentRaw();
        this.embedCount = msg.getEmbeds().size();
        this.channelId = msg.getChannelIdLong();
        this.timestamp = msg.getTimeCreated();

        this.attachmentUrls = msg.getAttachments().stream()
                .map(Message.Attachment::getUrl)
                .toList();

        this.attachmentNames = msg.getAttachments().stream()
                .map(Message.Attachment::getFileName)
                .toList();
    }

    public long getMessageId() {
        return messageId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public int getEmbedCount() {
        return embedCount;
    }

    public List<String> getAttachmentUrls() {
        return attachmentUrls;
    }

    public List<String> getAttachmentNames() {
        return attachmentNames;
    }

    public boolean isBot() {
        return Main.jda.getUserById(authorId).isBot();
    }

    public long getChannelId() {
        return channelId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
