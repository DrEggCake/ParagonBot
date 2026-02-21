package com.dreggcake.src.Listeners.ReloadCommand;

import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ConfigReload extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();

        if (event.getAuthor().isBot()) return;
        if (event.getMessage().getContentStripped().equals("!reload")) {
            if (!PermissionUtil.canManageConfig(member)) {
//                event.getMessage().reply("You do not have the required role").queue();
                return;
            }
            try {
                Config.reload();
                Utilities.loadRuntimeValues();
                event.getMessage().reply("Config reloaded successfully!").queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getMessage().reply("Something went wrong whilst reloading config").queue();


            }

        }
    }
}
