package com.dreggcake.src.AutoModService.AttachmentService;

import com.dreggcake.src.AutoModService.Common.AutoModBypassService;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Set;

public class AttachmentListener extends ListenerAdapter {

    // Channels where ONLY attachments are allowed
    private static final Set<Long> ATTACHMENT_ONLY_CHANNELS = Set.of(
            1460002169544773746L
    );

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember() == null) return;

        if (!inAttachmentOnlyChannel(event.getChannel())) return;
        if (AutoModBypassService.shouldBypassAttachments(event.getMember())) return;

        // If message HAS attachments → allow
        if (!event.getMessage().getAttachments().isEmpty()) return;

        // Otherwise delete silently
        event.getMessage().delete().queue();
    }

    private boolean inAttachmentOnlyChannel(MessageChannelUnion channel) {

        // Ignore DMs
        if (!channel.getType().isGuild())
            return false;

        // Only apply to normal text channels
        if (channel.getType() != ChannelType.TEXT)
            return false;

        return ATTACHMENT_ONLY_CHANNELS.contains(channel.getIdLong());
    }
}
