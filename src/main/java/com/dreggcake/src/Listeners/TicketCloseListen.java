package com.dreggcake.src.Listeners;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketCloseListen extends ListenerAdapter {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static String generateTranscript(TextChannel channel) {
        try {
            LinkedList<Message> messages = new LinkedList<Message>();
            for (Message message : channel.getHistory().retrievePast(100).complete()) {
                if (message.getAuthor().isBot()) continue;
                messages.addFirst(message);
            }
            StringBuilder transcriptBuilder = new StringBuilder();
            for (Message message : messages) {
                transcriptBuilder.append(message.getAuthor().getAsMention()).append(" (").append(message.getAuthor().getEffectiveName()).append(")").append(" [").append(message.getTimeCreated().atZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("]: ").append(message.getContentDisplay()).append("\n");
            }
            return transcriptBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static void sendTranscriptToLogChannel(String transcript, TextChannel channel, Member member) {
        if (Utilities.TICKET_LOG_CHANNEL == null) {
            member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Log channel not found. Unable to send transcript. \n Please forward this to a staff.").queue());
            return;
        }
        String fileName = "transcript-" + channel.getName() + ".txt";
        try {
            File transcriptFile = File.createTempFile("transcript", ".txt");
            Files.write(transcriptFile.toPath(), transcript.getBytes(), new OpenOption[0]);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Ticket closed.");
            embed.setDescription("Name: " + channel.getName());
            embed.addField("Closed by: ", member.getAsMention(), false);
            embed.addField("Created on:", TimeFormat.DATE_TIME_LONG.format(channel.getTimeCreated().toInstant()), false);
            embed.addField("Closed on:", TimeFormat.DATE_TIME_LONG.format(timestamp.toInstant()), false);
            ((MessageCreateAction) Utilities.TICKET_LOG_CHANNEL.sendMessageEmbeds(embed.build(), new MessageEmbed[0]).addFiles(FileUpload.fromData(transcriptFile, fileName))).queue();
            transcriptFile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {

        // Must be in a guild
        if (!e.isFromGuild()) return;

        // Must be a text channel (not thread, not news, not DM)
        if (!(e.getChannel() instanceof TextChannel)) return;

        TextChannel channel = (TextChannel) e.getChannel();

        // Must be inside a category
        if (channel.getParentCategory() == null) return;

        Member member = e.getMember();
        if (member == null) return;

        String message = e.getMessage().getContentRaw();

        // Only allow command inside ticket category
        if (!channel.getParentCategory().getId().equals(Utilities.TICKET_CATEGORY.getId())) {
            return;
        }

        // Permission check
        boolean hasPermission = member.getRoles().stream()
                .anyMatch(role -> role.getPosition() >= Utilities.SUPPORT_ROLE.getPosition());

        if (!message.equalsIgnoreCase("!close")) return;

        if (!hasPermission) {
            channel.sendMessage(
                    "Sorry, you don't have permission to do this. Please ping a staff member."
            ).queue();
            return;
        }

        // ===== VALID TICKET CLOSE =====
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Ticket Closing")
                .setDescription("This ticket #" + channel.getName() + " will be closed soon.")
                .setColor(0xFF0000);

        channel.sendMessageEmbeds(embed.build()).queue();

        String transcript = generateTranscript(channel);
        sendTranscriptToLogChannel(transcript, channel, member);
        scheduleTicketClosure(channel);
    }

    private void scheduleTicketClosure(TextChannel channel) {
        TextChannel currentChannel = channel.getGuild().getTextChannelById(channel.getId());
        if (channel.getParentCategory() == null || channel.getParentCategory().getIdLong() != Utilities.TICKET_CATEGORY.getIdLong()) {
            currentChannel.sendMessage("This command can only be used in the designated ticket category.").queue();
        } else {
            this.scheduler.schedule(() -> {
                if (currentChannel == null) {
                    currentChannel.sendMessage("Can't find channel").queue();
                } else {
                    try {
                        String delete = "DELETE FROM tickets WHERE ticketid = ?";
                        PreparedStatement pstmt = Main.conn.prepareStatement(delete);
                        pstmt.setString(1, channel.getId());
                        pstmt.executeUpdate();   // ← use executeUpdate()
                        pstmt.close();

                        currentChannel.delete()
                                .queue(
                                        success -> System.out.println(currentChannel.getName() + " deleted successfully."),
                                        error -> error.printStackTrace()
                                );

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, 5L, TimeUnit.SECONDS);
        }
    }
}