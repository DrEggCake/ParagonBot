package com.dreggcake.src.Counting;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static com.dreggcake.src.Main.jda;

public class CountingDAO {

    private static final Gson GSON = new Gson();
    private static final String DB_URL = "jdbc:sqlite:counting.db";

    public CountingDAO() {
        init();
    }

    private void init() {
        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement s = c.createStatement()) {

            s.execute("""
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
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = c.prepareStatement("""
                INSERT INTO counting_channels
                (channel_id, current_number, last_user_id, contributions)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(channel_id) DO UPDATE SET
                    current_number = excluded.current_number,
                    last_user_id = excluded.last_user_id,
                    contributions = excluded.contributions
             """)) {

            ps.setLong(1, state.getChannelId());
            ps.setLong(2, state.current());
            ps.setLong(3, state.getLastUserId());
            ps.setString(4, GSON.toJson(state.getContributions()));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, CountingState> loadAll() {
        Map<Long, CountingState> map = new HashMap<>();

        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM counting_channels")) {

            while (rs.next()) {
                long channelId = rs.getLong("channel_id");
                TextChannel channel = jda.getTextChannelById(channelId);

                CountingState state = new CountingState(channelId,channel);
                state.setCurrent(rs.getLong("current_number"));
                state.setLastUserId(rs.getLong("last_user_id"));

                Map<Long, Integer> contrib =
                        GSON.fromJson(
                                rs.getString("contributions"),
                                new TypeToken<Map<Long, Integer>>() {}.getType()
                        );

                state.getContributions().putAll(contrib);
                map.put(channelId, state);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }
}
