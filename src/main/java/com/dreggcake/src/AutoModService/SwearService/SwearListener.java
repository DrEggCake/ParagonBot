package com.dreggcake.src.AutoModService.SwearService;

import com.dreggcake.src.AutoModService.Common.AutoModBypassService;
import com.dreggcake.src.AutoModService.Common.AutoModCategoryBypassService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SwearListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember() == null) return;

        Member member = event.getMember();

        if (AutoModBypassService.shouldBypass(member))
            return;
        if (AutoModCategoryBypassService.shouldCategoryBypass(event.getMessage().getCategory()))
            return;
        if (!SwearService.containsSwear(event.getMessage().getContentRaw()))
            return;

        SwearPunishmentService.handleSwear(event.getMessage(), member);
    }
}
