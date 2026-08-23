package com.dreggcake.src.Miscellaneous.NSCommands;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.dreggcake.src.Utility.GuildContext.DREGGCAKE;

public class Set extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User owner = event.getGuild().getOwner().getUser();
        User user = event.getAuthor();


        if (event.getMessage().getContentRaw().equalsIgnoreCase("!setAvatar")) {
            if (!user.equals(DREGGCAKE) && !user.equals(owner)) return;
            Message message = event.getMessage();
            if (event.getMessage().getAttachments().isEmpty()) {
                message.reply("No image attached.").queue();
                return;
            }
            var attachment = message.getAttachments().getFirst();
            if (attachment.isImage()) {
                message.reply("Updating the avatar... Please wait").queue();
                setAvatarFromAttachment(attachment, event);
            } else {
                message.reply("Please attach image file!").queue();
            }

        }
    }

    private void setAvatarFromAttachment(Message.Attachment attachment, MessageReceivedEvent event) {
        // Get the URL of the attachment
        String url = attachment.getUrl();

        try {
            // Download the image to a temporary file
            File tempFile = File.createTempFile("avatar_", "." + attachment.getFileExtension());
            tempFile.deleteOnExit();  // Ensure the file gets deleted when the program exits

            // Open connection to the URL
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Check if the connection was successful
            if (connection.getResponseCode() == 200) {
                // Write the input stream to the temporary file
                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Set the bot's avatar
                event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(tempFile)).queue(
                        success -> event.getChannel().sendMessage("Avatar updated successfully!").queue(),
                        failure -> event.getChannel().sendMessage("Failed to set avatar.").queue()
                );
            } else {
                event.getChannel().sendMessage("Failed to download the attachment.").queue();
            }

        } catch (IOException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("An error occurred while setting the avatar.").queue();
        }
    }
}