package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.TicketSystem.TicketData;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TicketCloseListener extends ListenerAdapter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() == null) return;

        String content = event.getMessage().getContentRaw();

        if(content.equals("!close")){
            if(!(event.getChannel() instanceof TextChannel)) return;

            TextChannel channel = event.getChannel().asTextChannel();
            TicketData ticketData = TicketDAO.retrieveTicket(channel.getIdLong());

            Member member = event.getMember();

            if(member.getIdLong() != ticketData.userId() ||
                    !member.getRoles().contains(Utilities.STAFF_ROLE) ||
                    !member.hasPermission(Permission.ADMINISTRATOR)
            ) return;

            generateTranscript(channel, member);















        }


    }


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

        try {
            File transcriptFile = File.createTempFile("transcript", ".txt");
            Files.writeString(transcriptFile.toPath(), transcript);

            if (Utilities.TICKET_LOG_CHANNEL == null) {
                channel.getGuild().getOwner().getUser().openPrivateChannel().queue(
                        pc -> {
                            pc.sendMessage("Log channel not Found! Unable to send transcript. Details below\n"+
                                    channel.getName() + " " + channel.getId() + "\n" +
                                    member.getEffectiveName() + " " + member.getId()).addFiles(
                                            FileUpload.fromData(transcriptFile, "transcript-"+channel.getName()+".txt")
                            ).queue();
                        }
                );

            }

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
}
