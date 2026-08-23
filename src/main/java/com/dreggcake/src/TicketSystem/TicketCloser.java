package com.dreggcake.src.TicketSystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.dreggcake.src.Utility.Channels;

/**
 * The one way tickets end: transcript, log entry, DAO cleanup and channel deletion.
 */
public final class TicketCloser {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TicketCloser() {
    }

    /**
     * Posts a closing notice in the ticket, archives the transcript,
     * removes the ticket and deletes the channel after the given delay.
     */
    public static void close(TextChannel channel, String closedBy, String noticeDescription, long deleteDelaySeconds) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Ticket Closed");
        embed.setDescription(noticeDescription);
        embed.setColor(Color.RED);
        embed.setFooter("ParagonMC Ticket System");

        channel.sendMessageEmbeds(embed.build()).queue(
                success -> {
                    sendTranscript(channel, closedBy);
                    TicketDAO.deleteTicket(channel.getIdLong());
                    channel.delete().queueAfter(deleteDelaySeconds, TimeUnit.SECONDS);
                }
        );
    }

    /**
     * Builds the transcript of the last 100 messages (bots excluded)
     * and posts it to the ticket log channel.
     */
    public static void sendTranscript(TextChannel channel, String closedBy) {
        channel.getHistory().retrievePast(100).queue(messages -> {
            try {
                List<Message> filtered = new ArrayList<>(messages.size());

                for (Message msg : messages) {
                    if (!msg.getAuthor().isBot()) {
                        filtered.add(msg);
                    }
                }

                StringBuilder transcriptBuilder = new StringBuilder();

                for (int i = filtered.size() - 1; i >= 0; i--) {
                    Message message = filtered.get(i);

                    transcriptBuilder
                            .append(message.getAuthor().getAsMention())
                            .append(" (").append(message.getAuthor().getEffectiveName()).append(")")
                            .append(" [")
                            .append(message.getTimeCreated()
                                    .atZoneSameInstant(ZoneId.of("UTC"))
                                    .format(FORMATTER))
                            .append("]: ")
                            .append(message.getContentDisplay())
                            .append("\n");
                }

                sendTranscriptToLogChannel(transcriptBuilder.toString(), channel, closedBy);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void sendTranscriptToLogChannel(String transcript, TextChannel channel, String closedBy) {

        try {
            File transcriptFile = File.createTempFile("transcript", ".txt");
            Files.writeString(transcriptFile.toPath(), transcript);

            if (Channels.TICKET_LOG == null) {
                channel.getGuild().getOwner().getUser().openPrivateChannel().queue(
                        pc -> {
                            pc.sendMessage("Log channel not Found! Unable to send transcript. Details below\n" +
                                    channel.getName() + " " + channel.getId() + "\n" +
                                    "Closed by: " + closedBy).addFiles(
                                    FileUpload.fromData(transcriptFile, "transcript-" + channel.getName() + ".txt")
                            ).queue();
                        }
                );
                return;
            }

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Ticket closed.")
                    .setDescription("Name: " + channel.getName())
                    .addField("Closed by:", closedBy, false)
                    .addField("Created on:", TimeFormat.DATE_TIME_LONG.format(channel.getTimeCreated().toInstant()), false)
                    .addField("Closed on:", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);

            Channels.TICKET_LOG
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(transcriptFile, "transcript-" + channel.getName() + ".txt"))
                    .queue();

            transcriptFile.deleteOnExit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Convenience overload for human closers. */
    public static void close(TextChannel channel, Member member, String noticeDescription, long deleteDelaySeconds) {
        close(channel, member.getAsMention(), noticeDescription, deleteDelaySeconds);
    }
}
