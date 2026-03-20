package com.dreggcake.src.Listeners;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.time.Instant;

public class databaseBackup extends ListenerAdapter implements Runnable {
    private final JDA jda = Main.jda;
    String filePath = "database.db";


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
