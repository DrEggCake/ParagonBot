package com.dreggcake.src;

import com.dreggcake.src.Counting.CountingManager;
import com.dreggcake.src.Database.DatabaseBootstrap;
import com.dreggcake.src.Utility.Channels;
import com.dreggcake.src.Utility.GuildContext;
import com.dreggcake.src.Utility.Roles;
import com.dreggcake.src.Utility.RuntimeValues;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

/**
 * Runs once when JDA is ready: bootstraps databases, loads Discord registries
 * and starts recurring background tasks.
 */
public class BotStartupListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        if (!initDatabases()) return;
        initDiscordObjects(event);
        if (GuildContext.GUILD == null) {
            System.out.println("Guild not found!");
            return;
        }
        CountingManager.restore();
        RecurringTasks.start();
    }

    private boolean initDatabases() {
        try {
            DatabaseBootstrap.initialize();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // stop if DB fails
        }
    }

    private void initDiscordObjects(ReadyEvent event) {
        GuildContext.init(event.getJDA());
        Roles.load(GuildContext.GUILD);
        Channels.load(GuildContext.GUILD);
        RuntimeValues.reload();
    }
}
