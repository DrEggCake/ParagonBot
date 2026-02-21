package com.dreggcake.src;

import com.dreggcake.src.Counting.CountingDAO;
import com.dreggcake.src.Counting.CountingManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class onReady extends ListenerAdapter {
    public static final Map<String, Integer> inviteCache = new HashMap<>();


    @Override
    public void onReady(ReadyEvent event) {
        Utilities.init(event.getJDA());
        Utilities.loadRuntimeValues();

        Guild guild = Utilities.GUILD;

        if (guild == null) {
            System.out.println("Guild not found!");
            return;
        }

        guild.retrieveInvites().queue(invites -> {
            inviteCache.clear();
            for (Invite invite : invites) {
                inviteCache.put(invite.getCode(), invite.getUses());
            }
            System.out.println("Invite cache loaded: " + inviteCache.size());
        });
        CountingDAO dao = new CountingDAO();

        dao.loadAll().forEach((channelId, state) -> {
            CountingManager.enable(channelId, state);
        });
    }
}



