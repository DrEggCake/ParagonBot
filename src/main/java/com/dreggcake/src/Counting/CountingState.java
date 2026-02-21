package com.dreggcake.src.Counting;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class CountingState {

    private final long channelId;
    private final TextChannel channel;
    private long currentNumber;
    private long lastUserId;

    private final Map<Long, Integer> contributions = new HashMap<>();

    public CountingState(long channelId, TextChannel channel) {
        this.channelId = channelId;
        this.channel = channel;
        this.currentNumber = 0;
        this.lastUserId = -1;
    }
    // 🔹 NEW: read-only getter
    public long current() {
        return currentNumber;
    }

    // 🔹 NEW: increments and returns new value
    public long increment() {
        return ++currentNumber;
    }

    // 🔹 NEW: reset safely
    public void reset() {
        this.currentNumber = 0;
        this.lastUserId = -1;
    }

    // existing getters/setters
    public long getChannelId() {
        return channelId;
    }

    public long getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(long lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Map<Long, Integer> getContributions() {
        return contributions;
    }

    // admin-only setter
    public void setCurrent(long value) {
        this.currentNumber = value;
    }

    public TextChannel getChannel() {
        return channel;
    }
}
