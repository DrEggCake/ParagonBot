package com.dreggcake.src.Counting;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.time.Instant;

public class CountingDatabaseBackup extends ListenerAdapter implements Runnable {
    String filePath = "counting.db";


    public void run() {
        Utilities.DREGGCAKE.openPrivateChannel().queue((channel) -> {
            File databaseFile = new File(filePath);

            if (databaseFile.exists()) {
                long epochTime = Instant.now().getEpochSecond();
                String message = "Here is the latest database backup.\nSent at: <t:" + epochTime + ":F>";

                channel.sendMessage(message).addFiles(FileUpload.fromData(databaseFile)).queue();
            } else {
                channel.sendMessage("DAO not found.").queue();
            }

        });
    }

}
