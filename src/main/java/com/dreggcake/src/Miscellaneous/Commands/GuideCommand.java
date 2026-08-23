package com.dreggcake.src.Miscellaneous.Commands;

import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
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
                "This is an extensive guide about server.\n" +
                        "Choose a type to get started."
        );

        Button minecraftButton = Button.primary("minecraftSection", "Minecraft");
        Section minecraftSection = Section.of(
                minecraftButton,
                TextDisplay.of("## Minecraft Information"),
                TextDisplay.of("Find In-game related information.")
        );

        Button supportButton = Button.link("https://discord.com/channels/1404254526503915683/1404676057386324019", "Support");
        Section supportSection = Section.of(
                supportButton,
                TextDisplay.of("## Support Information"),
                TextDisplay.of(
                        "We provide support for all sorts of server related issues to our members.\n" +
                                "Whether you encounter a cheater, a bug, or just have a general question, " +
                                "our expert staff team will help you with all sorts of issues.\n\n" +
                                "To visit the support channel, press the button on the right."
                )
        );

        Button partnerButton = Button.primary("partnerSection", "Partner");
        Section partnerSection = Section.of(
                partnerButton,
                TextDisplay.of("## Partner Information"),
                TextDisplay.of( "We partner with other servers/games to strengthen our communities and provide mutual benefit.\n"+
                        "Learn more about our awesome partners and their amazing projects.")
        );

        Container container = Container.of(
                title,
                description,
                minecraftSection,
                Separator.createDivider(Separator.Spacing.SMALL),
                supportSection,
                Separator.createDivider(Separator.Spacing.SMALL),
                partnerSection
        );


        event.replyComponents(container).useComponentsV2().queue();
    }


}
