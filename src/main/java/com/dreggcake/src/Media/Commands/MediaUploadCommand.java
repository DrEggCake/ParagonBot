package com.dreggcake.src.Media.Commands;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Media.MediaPermissions;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.List;

public class MediaUploadCommand implements ICommand {


    @Override
    public String getName() {
        return "media";
    }

    @Override
    public String getDescription() {
        return "Upload a video";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "link", "Link of your video", true)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        Member member = event.getMember();
        if (!MediaPermissions.canUpload(member)) {
            event.reply("You are not allowed to use this command!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String link = event.getOption("link").getAsString();

        if (!isValidLink(link)) {
            event.reply("Please provide a valid YouTube or TikTok link.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        VideoType videoType = videoType(link);

        String type = switch (videoType) {
            case YOUTUBE -> "Youtube";
            case TIKTOK -> "Tiktok";
        };

        MessageCreateBuilder message = new MessageCreateBuilder();
        message.addContent(":camera_with_flash: " +
                event.getMember().getAsMention() +
                " has just uploaded a new " + type + " video! \n");
        message.addContent(link);

        Utilities.MEDIA_UPLOAD_CHANNEL.sendMessage(message.build()).queue();
    }

    private boolean isValidLink(String link) {
        link = link.toLowerCase();

        return link.contains("youtube.com") ||
                link.contains("youtu.be") ||
                link.contains("tiktok.com");
    }

    private VideoType videoType(String link) {
        if (link.contains("youtube.com") || link.contains("youtu.be")) {
            return VideoType.YOUTUBE;
        } else if (link.contains("tiktok.com")) {
            return VideoType.TIKTOK;
        }
        return null;
    }

    enum VideoType {
        YOUTUBE,
        TIKTOK
    }
}
