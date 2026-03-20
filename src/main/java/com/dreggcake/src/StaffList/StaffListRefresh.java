package com.dreggcake.src.StaffList;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.List;

public class StaffListRefresh implements Runnable {
    @Override
    public void run() {
        TextChannel staffListChannel = Utilities.STAFF_LIST_CHANNEL;
        StaffListBuild staffListBuild = new StaffListBuild();

        long selfId = Main.jda.getSelfUser().getIdLong();

        staffListChannel.getHistory().retrievePast(100).queue(history -> {

            List<Message> retrievedHistory = history.stream()
                    .filter(msg -> msg.getAuthor().getIdLong() == selfId)
                    .sorted((a, b) -> a.getTimeCreated().compareTo(b.getTimeCreated()))
                    .toList();

            List<MessageCreateBuilder> staffList = staffListBuild.buildList();

            for (int i = 0; i < staffList.size(); i++) {

                if (i >= retrievedHistory.size()) {
                    // Send new message
                    staffListChannel.sendMessage(staffList.get(i).build())
                            .useComponentsV2()
                            .queue();
                    continue;
                }

                // Edit existing message
                retrievedHistory.get(i)
                        .editMessageComponents(staffList.get(i).getComponents())
                        .queue();
            }
        });
    }
}
