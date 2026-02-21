package com.dreggcake.src.AutoModService.Common;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.Set;

public final class AutoModChannelBypassService {

    // Channels where AutoMod should NOT apply
    // e.g. counting, spam, bot-commands
    private static final Set<Long> BYPASS_CHANNEL_IDS = Set.of(
            Utilities.COUNTING_CHANNEL.getIdLong(),
            Utilities.SPAM_CHANNEL.getIdLong()
    );

    private AutoModChannelBypassService() {
        // Utility class
    }

    /**
     * @return true if automod should be skipped in this channel
     */
    public static boolean shouldBypass(MessageChannelUnion channel) {
        if (channel == null)
            return true;

        return BYPASS_CHANNEL_IDS.contains(channel.getIdLong());
    }
}
