package com.dreggcake.src.Counting;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class CountingService {
    private final CountingDAO dao = new CountingDAO();

    public void handle(MessageReceivedEvent event, TextChannel channel, CountingState state) {
        String raw = event.getMessage().getContentRaw().trim();
        long userId = event.getAuthor().getIdLong();

        // 1️⃣ Ignore commands
        if (raw.startsWith("!") || raw.startsWith("/")) return;

        // 2️⃣ Not a number at all
        if (!raw.matches("\\d+")) {
            deleteWithWarning(
                    event,
                    channel,
                    ":warning: Only numbers are allowed " + event.getAuthor().getAsMention()
            );
            return;
        }

        long input = Long.parseLong(raw);

        // 3️⃣ Same user twice
        if (state.getLastUserId() == userId) {
            deleteWithWarning(
                    event,
                    channel,
                    ":warning: You must wait for another user to type the next number, "
                            + event.getAuthor().getAsMention() + "!"
            );
            return;
        }

        // 4️⃣ Wrong number (number but incorrect)
        if (input != state.current() + 1) {
            deleteWithWarning(event,channel,":warning: Incorrect number, Current number is: **"+state.current()+"**");
            return;
        }

        // ✅ SUCCESS
        state.increment();
        state.setLastUserId(userId);
        state.getContributions().merge(userId, 1, Integer::sum);
        dao.save(state);


        event.getMessage()
                .addReaction(Emoji.fromUnicode("✅"))
                .queue();

//        updateTopic(channel, state);
    }


    /* ---------------- UTIL ---------------- */

    private void deleteWithWarning(MessageReceivedEvent event,
                                   TextChannel channel,
                                   String warning) {

        // delete user's message
        event.getMessage().delete().queue();

        // send warning & auto-delete after 3s
        channel.sendMessage(warning)
                .delay(Duration.ofSeconds(3))
                .flatMap(Message::delete)
                .queue();
    }

    /* ---------------- ADMIN ---------------- */

    public void setCounter(TextChannel channel, CountingState state, long value) {
        state.setCurrent(value);
        state.setLastUserId(-1);
        dao.save(state);


        channel.sendMessage("🔢 Counter manually set to **" + value + "**")
                .delay(Duration.ofSeconds(3))
                .flatMap(Message::delete)
                .queue();

//        updateTopic(channel, state);
    }

    /* ---------------- TOPIC ---------------- */

    public void updateTopic(TextChannel channel, CountingState state) {
        String leaderboard = state.getContributions().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> "<@" + e.getKey() + ">: " + e.getValue())
                .collect(Collectors.joining(" | "));

        String topic =
                "🔢 Current: " + state.current() +
                        " | 🏆 Top counters: " +
                        (leaderboard.isEmpty() ? "None yet" : leaderboard);

        channel.getManager()
                .setTopic(topic.substring(0, Math.min(1024, topic.length())))
                .queue();
    }
}
