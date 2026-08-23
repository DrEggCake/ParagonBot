package com.dreggcake.src.InviteTracker;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;

import java.util.HashMap;
import java.util.Map;

/**
 * Snapshot of the guild's current invites, used to attribute joins.
 */
public class InviteCache {

    private final Map<String, Invite> invites = new HashMap<>();

    public void refresh(Guild guild) {
        if (guild == null) return;
        guild.retrieveInvites().queue(fresh -> {
            synchronized (invites) {
                invites.clear();
                for (Invite invite : fresh) {
                    invites.put(invite.getCode(), invite);
                }
            }
        });
    }

    public boolean isKnown(String code) {
        synchronized (invites) {
            return invites.containsKey(code);
        }
    }

    /**
     * @return tracked uses of the invite, or -1 if unknown
     */
    public int uses(String code) {
        synchronized (invites) {
            Invite invite = invites.get(code);
            return invite == null ? -1 : invite.getUses();
        }
    }
}
