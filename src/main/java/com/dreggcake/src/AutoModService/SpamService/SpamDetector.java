package com.dreggcake.src.AutoModService.SpamService;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpamDetector {

    // === CONFIG ===
    private static final int MAX_MESSAGES = 5;          // max messages
    private static final long TIME_WINDOW_MS = 5_000;   // 5 seconds

    private static final int DUPLICATE_LIMIT = 3;       // same msg count
    private static final long DUPLICATE_WINDOW_MS = 10_000;

    // === DATA ===
    private final Map<Long, Deque<Long>> messageTimestamps = new ConcurrentHashMap<>();
    private final Map<Long, Deque<MessageEntry>> messageHistory = new ConcurrentHashMap<>();

    /**
     * Main method to call in  listener
     */
    public boolean isSpam(long userId, String content) {
        long now = System.currentTimeMillis();

        // Rate spam
        if (isRateSpam(userId, now)) {
            return true;
        }

        // Duplicate / similar spam
        if (isDuplicateSpam(userId, content, now)) {
            return true;
        }

        return false;
    }

    // ================= RATE SPAM =================

    private boolean isRateSpam(long userId, long now) {
        Deque<Long> timestamps =
                messageTimestamps.computeIfAbsent(userId, k -> new ArrayDeque<>());

        timestamps.addLast(now);

        // Remove old timestamps
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > TIME_WINDOW_MS) {
            timestamps.pollFirst();
        }

        return timestamps.size() > MAX_MESSAGES;
    }

    // ================= DUPLICATE SPAM =================

    private boolean isDuplicateSpam(long userId, String content, long now) {
        String normalized = normalize(content);

        Deque<MessageEntry> history =
                messageHistory.computeIfAbsent(userId, k -> new ArrayDeque<>());

        history.addLast(new MessageEntry(normalized, now));

        // Remove old messages
        while (!history.isEmpty() && now - history.peekFirst().timestamp > DUPLICATE_WINDOW_MS) {
            history.pollFirst();
        }

        int matches = 0;
        for (MessageEntry entry : history) {
            if (entry.content.equals(normalized)) {
                matches++;
            }
        }

        return matches >= DUPLICATE_LIMIT;
    }

    // ================= NORMALIZATION =================

    private String normalize(String input) {
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "") // remove symbols/emojis
                .replaceAll("\\s+", " ")      // collapse spaces
                .trim();
    }

    // ================= HELPER =================

    private static class MessageEntry {
        private final String content;
        private final long timestamp;

        private MessageEntry(String content, long timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}
