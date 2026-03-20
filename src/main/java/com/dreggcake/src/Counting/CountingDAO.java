package com.dreggcake.src.Counting;

import com.dreggcake.src.Database.Databases;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.dreggcake.src.Main.jda;

public class CountingDAO {

    private static final Gson GSON = new Gson();

    public CountingDAO() {
        init();
    }

    private void init() {
        try {
            Databases.getCountingDatabase().update("""
                CREATE TABLE IF NOT EXISTS counting_channels (
                    channel_id INTEGER PRIMARY KEY,
                    current_number INTEGER NOT NULL,
                    last_user_id INTEGER NOT NULL,
                    contributions TEXT NOT NULL
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(CountingState state) {
        try {
            Databases.getMainDatabase().update("""
                INSERT INTO counting_channels
                (channel_id, current_number, last_user_id, contributions)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(channel_id) DO UPDATE SET
                    current_number = excluded.current_number,
                    last_user_id = excluded.last_user_id,
                    contributions = excluded.contributions
            """,
                    state.getChannelId(),
                    state.current(),
                    state.getLastUserId(),
                    GSON.toJson(state.getContributions())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, CountingState> loadAll() {
        try {
            return Databases.getMainDatabase().query(
                    "SELECT * FROM counting_channels",
                    rs -> {
                        Map<Long, CountingState> map = new HashMap<>();

                        while (rs.next()) {
                            long channelId = rs.getLong("channel_id");
                            TextChannel channel = jda.getTextChannelById(channelId);

                            CountingState state = new CountingState(channelId, channel);
                            state.setCurrent(rs.getLong("current_number"));
                            state.setLastUserId(rs.getLong("last_user_id"));

                            Map<Long, Integer> contrib =
                                    GSON.fromJson(
                                            rs.getString("contributions"),
                                            new TypeToken<Map<Long, Integer>>() {}.getType()
                                    );

                            if (contrib != null) {
                                state.getContributions().putAll(contrib);
                            }

                            map.put(channelId, state);
                        }

                        return map;
                    }
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}