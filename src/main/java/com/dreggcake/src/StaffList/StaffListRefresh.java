package com.dreggcake.src.StaffList;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class StaffListRefresh implements Runnable {

    private final StaffMessageBuilder messageBuilder = new StaffMessageBuilder();


    /**
     * Edit all existing staff list messages to sync with the latest roles.
     */
    private void editStaffList(List<Message> messages) {
        TextChannel staffListChannel = Utilities.STAFF_LIST_CHANNEL;

        // Build the new messages
        List<Container> updatedMessages = messageBuilder.buildAllMessages(staffListChannel.getGuild());

        // Ensure we don't exceed existing messages
        int minSize = Math.min(messages.size(), updatedMessages.size());

        // Edit existing messages
        for (int i = 0; i < minSize; i++) {
            Message oldMessage = messages.get(i);
            Container newContent = updatedMessages.get(i);

            oldMessage.editMessageComponents(newContent).useComponentsV2().queue();
        }

        // If there are more updated messages than existing, send the extra
        for (int i = minSize; i < updatedMessages.size(); i++) {
            staffListChannel.sendMessageComponents(updatedMessages.get(i)).useComponentsV2().queue();
        }

        // If there are more old messages than needed, delete the extra
        for (int i = minSize; i < messages.size(); i++) {
            messages.get(i).delete().queue();
        }
    }

    /**
     * Retrieve all messages in the staff list channel sent by the bot
     */
    private List<Message> retrieveStaffList() {
        TextChannel staffListChannel = Utilities.STAFF_LIST_CHANNEL;

        List<Message> allMessages = staffListChannel.getHistory().retrievePast(100).complete();
        List<Message> staffList = allMessages.stream()
                .filter(msg -> msg.getAuthor().equals(staffListChannel.getJDA().getSelfUser()))
                .toList();

        return staffList;
    }

    @Override
    public void run() {
        try {
            List<Message> messages = retrieveStaffList();
            editStaffList(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}