package com.dreggcake.src.Embeds;

import com.dreggcake.src.ICommand;
import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;

public class InfoEmbedCommand implements ICommand {

    @Override
    public String getName() {
        return "embed-info";
    }

    @Override
    public String getDescription() {
        return "Sends information embed in the channel command is used.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        event.deferReply(true).queue();

        Member member = event.getMember();
        if (member == null || event.getGuild() == null) {
            event.getHook()
                    .sendMessage("This command can only be used in a server.")
                    .queue();
            return;
        }

        Role managerRole = event.getGuild().getRoleById(Utilities.MANAGER_ROLE.getId());
        if (managerRole == null) {
            event.getHook()
                    .sendMessage("Admin role not found. Please check configuration.")
                    .queue();
            return;
        }

        boolean hasPermission = member.getRoles().stream()
                .anyMatch(role -> role.getPosition() >= managerRole.getPosition());

        if (!hasPermission) {
            event.getHook()
                    .sendMessage("❌ You do not have permission to use this command.")
                    .queue();
            return;
        }

        sendEmbed(event);

        event.getHook()
                .editOriginal("✅ Embed sent successfully!")
                .queue();
    }

    private void sendEmbed(SlashCommandInteractionEvent event) {

        String description =
                "Welcome to our community! Below you'll find important links for applications, server info, and more.\n" +

                        "## :link: Important Links\n" +
                        "- [Apply for Staff](https://docs.google.com/forms/d/e/1FAIpQLSdrt6O0-xu-M0t6j7XjCY2QbORAVmViJmN816uUWJXpGA3Bqg/viewform)\n" +
                        "- [Server Store](" + Utilities.STORE + ")\n" +
                        "- [Server Rules](https://docs.google.com/document/d/1dHlB99Z8CH49B0DKjXySQEfV8fmOERc8hoqpTCuhuJg/edit?usp=sharing)\n" +

                        "## :star2: Vote Links\n" +
                        "Support the server and earn rewards by voting for us!\n" +
                        "- [Vote Link #1](https://www.minerank.com/paragon-network/vote)\n" +
                        "- [Vote Link #2](https://minecraftservers.blog/server/paragon-network/vote)\n" +
                        "- [Vote Link #3](https://minecraftservers.org/vote/682220)\n" +
                        "- [Vote Link #4](https://play-minecraft-servers.com/minecraft-servers/paragon-network/?tab=vote)\n" +
                        "- [Vote Link #5](https://minecraft.global/servers/05cb77c2-c392-444a-acf7-717705bcdfea/vote)\n" +
                        "- [Vote Link #6](https://minecraft-serverlist.com/server/4015/vote)\n" +
                        "- [Vote Link #7](https://minecraft.buzz/vote/paragon-network)\n" +
                        "- [Vote Link #8](https://topminecraftservers.org/vote/42499)\n" +
                        "- [Vote Link #9](https://best-minecraft-servers.co/server-paragon-network.32062/vote)\n" +
                        "- [Vote Link #10](https://www.planetminecraft.com/server/paragon-network-6822468/vote/)\n" +

                        "## :tickets: Ticket Support\n" +
                        "Need support? Open a ticket for:\n" +
                        "- **General Support:** For any general inquiries or assistance.\n" +
                        "- **Appeal Support:** Want to appeal a ban or punishment? Reach out here.\n" +
                        "- **Purchase Support:** Questions about related to our online store.\n" +
                        "- **Password Support:** Having issues with logging in? We're here to help.\n" +
                        "- **Report Support:** Found a troublemaker? Report them here. \n" +
                        "- **And more...**\n" +
                        "Find ticket support here: <#1404676057386324019>\n" +

                        "## :scroll: Rules\n" +
                        "Our server rules ensure a fun and fair experience for all the players.\n" +
                        "Please take a moment to read them to avoid any issues or punishments.\n" +
                        "Full list of rules is available [HERE](https://docs.google.com/document/d/1dHlB99Z8CH49B0DKjXySQEfV8fmOERc8hoqpTCuhuJg/edit?usp=sharing).";


        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("WELCOME TO PARAGON NETWORK!")
                .setColor(new Color(39, 64, 253))
                .setDescription(description);


        event.getChannel()
                .sendMessageEmbeds(embed.build())
                .queue();
    }
}
