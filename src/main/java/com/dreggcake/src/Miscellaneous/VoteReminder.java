package com.dreggcake.src.Miscellaneous;

import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Utility.GuildContext;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Posts the vote reminder embed once a day at noon (server local time).
 */
public class VoteReminder implements Runnable {

    /* ================= Timings ================= */

    /**
     * Hour of day the reminder fires.
     */
    public static final int REMINDER_HOUR = 12;

    @Override
    public void run() {
        TextChannel channel = resolveChannel();
        if (channel == null) return;

        channel.sendMessageEmbeds(VoteEmbeds.embed())
                .addComponents(VoteEmbeds.rows())
                .queue();
    }

    private TextChannel resolveChannel() {
        String channelId = Config.getString("ChannelsID.voteReminderID", "");
        if (channelId == null || channelId.isBlank()) return null;

        return GuildContext.JDA.getTextChannelById(channelId);
    }
}
