package com.dreggcake.src.Miscellaneous;

import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Utility.GuildContext;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Periodically posts the vote reminder embed into the configured channel.
 */
public class VoteReminder implements Runnable {

    /* ================= Timings ================= */
    public static final long INTERVAL = 6; // hours

    @Override
    public void run() {
        TextChannel channel = resolveChannel();
        if (channel == null) return;

        channel.sendMessageEmbeds(VoteEmbeds.embed())
                .addComponents(ActionRow.of(VoteEmbeds.buttons()))
                .queue();
    }

    private TextChannel resolveChannel() {
        String channelId = Config.getString("ChannelsID.voteReminderID", "");
        if (channelId == null || channelId.isBlank()) return null;

        return GuildContext.JDA.getTextChannelById(channelId);
    }
}
