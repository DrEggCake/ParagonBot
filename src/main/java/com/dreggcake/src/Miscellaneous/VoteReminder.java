package com.dreggcake.src.Miscellaneous;

import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Utility.GuildContext;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Posts the vote reminder embed into the configured channel at fixed times
 * of day (00:00, 06:00, 12:00, 18:00 ... local server time).
 */
public class VoteReminder implements Runnable {

    /* ================= Timings ================= */

    /**
     * Reminder interval in hours; also defines the clock boundaries.
     */
    public static final int INTERVAL_HOURS = 6;

    /**
     * Millis until the next boundary (e.g. next 06:00 from a 02:13 start).
     */
    public static long initialDelayMillis() {
        ZonedDateTime now = ZonedDateTime.now();
        int periodsPassed = now.getHour() / INTERVAL_HOURS;
        ZonedDateTime next = now.toLocalDate().atStartOfDay(now.getZone())
                .plusHours((long) (periodsPassed + 1) * INTERVAL_HOURS);
        return Math.max(0, Duration.between(now, next).toMillis());
    }

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
