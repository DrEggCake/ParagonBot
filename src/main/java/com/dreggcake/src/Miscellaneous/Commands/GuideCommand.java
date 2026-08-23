package com.dreggcake.src.Miscellaneous.Commands;

import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.InputStream;
import java.util.List;

public class GuideCommand implements ICommand {

    int totalPages = 0;

    @Override
    public String getName() {
        return "guide";
    }

    @Override
    public String getDescription() {
        return "Get all guides related to server.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "page", "Get a specific page.")
                        .setMinValue(0)
                        .setMaxValue(100)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        TextDisplay title = TextDisplay.of("# Server Guide");
        TextDisplay description = TextDisplay.of(
                "This is an extensive guide about most things on our discord server.\n" +
                        "Choose a type to get started."
        );
        InputStream input = getClass().getResourceAsStream("/serverLogo.png");
        if (input == null)
            throw new IllegalStateException("serverLogo.png not found");

        FileUpload upload = FileUpload.fromData(input, "serverLogo.png");
        Thumbnail thumbnail = Thumbnail.fromFile(upload);

        Section thumbnailSection = Section.of(
                thumbnail,
                TextDisplay.of("")
        );

        Button minecraftButton = Button.primary("minecraftSection", "Minecraft");
        Section minecraftSection = Section.of(
                minecraftButton,
                TextDisplay.of("## Minecraft Information"),
                TextDisplay.of("Find In-game related information.")
        );

        Button supportButton = Button.primary("supportSection", "Support");
        Section supportSection = Section.of(
                supportButton,
                TextDisplay.of("## Support Information"),
                TextDisplay.of("We provide support for all sorts of server related issues to our members"),
                TextDisplay.of("Whether you encounter a cheater, a bug, or just have a general question"),
                TextDisplay.of("Our expert staff team will help you with all sorts of issues"),
                TextDisplay.of("To visit support channel, press the button on the right.")
        );

        Container container = Container.of(
                title,
                description,
                thumbnailSection,
                minecraftSection,
                supportSection
        );


        event.replyComponents(container).useComponentsV2().queue();
    }


}
