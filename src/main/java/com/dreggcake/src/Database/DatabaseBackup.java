package com.dreggcake.src.Database;

import com.dreggcake.src.Utility.GuildContext;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.time.Instant;

/**
 * Sends a snapshot of one database file to the bot owner via DM.
 */
public class DatabaseBackup implements Runnable {

    private final String filePath;

    public DatabaseBackup(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        GuildContext.DREGGCAKE.openPrivateChannel().queue((channel) -> {
            File databaseFile = new File(filePath);

            if (databaseFile.exists()) {
                long epochTime = Instant.now().getEpochSecond();
                String message = "Here is the latest database backup.\nSent at: <t:" + epochTime + ":F>";

                channel.sendMessage(message).addFiles(FileUpload.fromData(databaseFile)).queue();
            } else {
                channel.sendMessage("Backup failed: file not found: " + filePath).queue();
            }
        });
    }
}
