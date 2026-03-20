package com.dreggcake.src.Listeners;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketCloseListen extends ListenerAdapter {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static void generateTranscript(TextChannel channel, Member member) {
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

                sendTranscriptToLogChannel(transcriptBuilder.toString(), channel, member);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static void sendTranscriptToLogChannel(String transcript, TextChannel channel, Member member) {
        if (Utilities.TICKET_LOG_CHANNEL == null) {
            member.getUser().openPrivateChannel()
                    .queue(pc -> pc.sendMessage("Log channel not found. Unable to send transcript.\nPlease forward this to staff.").queue());
            return;
        }

        try {
            File transcriptFile = File.createTempFile("transcript", ".txt");
            Files.writeString(transcriptFile.toPath(), transcript);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Ticket closed.")
                    .setDescription("Name: " + channel.getName())
                    .addField("Closed by:", member.getAsMention(), false)
                    .addField("Created on:", TimeFormat.DATE_TIME_LONG.format(channel.getTimeCreated().toInstant()), false)
                    .addField("Closed on:", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);

            Utilities.TICKET_LOG_CHANNEL
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(transcriptFile, "transcript-" + channel.getName() + ".txt"))
                    .queue();

            transcriptFile.deleteOnExit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {

        if (!e.isFromGuild()) return;
        if (!(e.getChannel() instanceof TextChannel channel)) return;

        if (channel.getParentCategory() == null) return;

        Member member = e.getMember();
        if (member == null) return;

        if (!e.getMessage().getContentRaw().equalsIgnoreCase("!close")) return;

        if (!channel.getParentCategory().getId().equals(Utilities.TICKET_CATEGORY.getId())) return;

        boolean hasPermission = member.getRoles().stream()
                .anyMatch(role -> role.getPosition() >= Utilities.SUPPORT_ROLE.getPosition());

        if (!hasPermission) {
            channel.sendMessage("Sorry, you don't have permission to do this. Please ping a staff member.").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Ticket Closing")
                .setDescription("This ticket #" + channel.getName() + " will be closed soon.")
                .setColor(0xFF0000);

        channel.sendMessageEmbeds(embed.build()).queue();

        generateTranscript(channel, member);
        scheduleTicketClosure(channel);
    }

    private void scheduleTicketClosure(TextChannel channel) {

        scheduler.schedule(() -> {

            TextChannel currentChannel = channel.getGuild().getTextChannelById(channel.getId());
            if (currentChannel == null) return;

            try {
                // 🔥 CLEAN DAO USAGE
                Databases.getMainDatabase().update("DELETE FROM tickets WHERE ticketid = ?", channel.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            currentChannel.delete().queue(
                    success -> System.out.println(currentChannel.getName() + " deleted successfully."),
                    Throwable::printStackTrace
            );

        }, 5L, TimeUnit.SECONDS);
    }
}