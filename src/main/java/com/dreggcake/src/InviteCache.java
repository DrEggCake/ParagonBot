package com.dreggcake.src;

import com.dreggcake.src.Listeners.InviteTracker;

public class InviteCache implements Runnable {

    private final InviteTracker tracker;

    public InviteCache(InviteTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void run() {
        tracker.cacheInvites();
    }
}
