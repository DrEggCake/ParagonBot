package com.dreggcake.src.AutoModService.Common;

import net.dv8tion.jda.api.entities.channel.concrete.Category;

import java.util.Set;
import com.dreggcake.src.Utility.Channels;

public final class AutoModCategoryBypassService {

    // Channels where AutoMod should NOT apply
    // e.g. counting, spam, bot-commands
    private static final Set<Long> BYPASS_CATEGORY_IDS = Set.of(
            Channels.TICKET_CATEGORY.getIdLong(),
            Channels.STAFF_LOGS_CATEGORY.getIdLong()
    );

    private AutoModCategoryBypassService() {
        // Utility class
    }

    /**
     * @return true if automod should be skipped in this category
     */
    public static boolean shouldCategoryBypass(Category category) {
        if (category == null)
            return true;

        return BYPASS_CATEGORY_IDS.contains(category.getIdLong());
    }
}
