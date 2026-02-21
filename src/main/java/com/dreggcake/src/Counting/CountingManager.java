package com.dreggcake.src.Counting;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dreggcake.src.Main.jda;

public class CountingManager {

    private static final Map<Long, CountingState> STATES = new ConcurrentHashMap<>();

    public static CountingState get(long channelId) {
        return STATES.get(channelId);
    }

    public static void enable(long channelId) {
        TextChannel channel = jda.getTextChannelById(channelId);
        STATES.putIfAbsent(channelId, new CountingState(channelId,channel));
    }

    public static void enable(long channelId,CountingState state) {
        STATES.put(channelId, state);
    }

    public static void disable(long channelId) {
        STATES.remove(channelId);
    }

    public static boolean isEnabled(long channelId) {
        return STATES.containsKey(channelId);
    }

    public static Map<Long, CountingState> all() {
        return STATES;
    }
}
