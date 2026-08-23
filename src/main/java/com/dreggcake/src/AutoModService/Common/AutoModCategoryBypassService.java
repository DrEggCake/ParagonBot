package com.dreggcake.src.AutoModService.Common;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

import java.util.Set;

public final class AutoModCategoryBypassService {

    // Channels where AutoMod should NOT apply
    // e.g. counting, spam, bot-commands
    private static final Set<Long> BYPASS_CATEGORY_IDS = Set.of(
            Utilities.TICKET_CATEGORY.getIdLong(),
            Utilities.STAFF_LOGS.getIdLong()
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
