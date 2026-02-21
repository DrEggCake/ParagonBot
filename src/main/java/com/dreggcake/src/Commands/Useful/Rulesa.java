/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src.Commands.Useful;

import com.dreggcake.src.ICommand;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public class Rulesa
        implements ICommand {
    @Override
    public String getName() {
        return "server-rules";
    }

    @Override
    public String getDescription() {
        return "Sends the server info embed.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "channel", "Channel ID you want to send the message in. None = this", false));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Role jradmin = event.getGuild().getRoleById("1251190911493341204");
        if (!member.getRoles().contains(jradmin)) {
            event.reply("You cannot do that command.").setEphemeral(true).queue();
            return;
        }
        String id = event.getOption("channel") == null ? event.getChannelId() : event.getOption("channel").getAsString();
        TextChannel channel = event.getJDA().getTextChannelById(id);
        if (channel == null) {
            event.reply("The channel specified does not exist.").setEphemeral(true).queue();
        }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Server Rules");
        embed.setImage(event.getGuild().getBannerUrl());
        embed.setThumbnail(event.getGuild().getIconUrl());
        embed.setDescription("Please take a moment to review our rules and guidelines to ensure a positive and enjoyable experience for everyone. \n By participating in this server, you agree to follow these rules.");
        EmbedBuilder generalRules = getEmbedBuilder();
        EmbedBuilder mcRules = getMcRules();
        channel.sendMessageEmbeds(embed.build(), generalRules.build(), mcRules.build()).queue();
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder() {
        EmbedBuilder generalRules = new EmbedBuilder();
        generalRules.setDescription("## General Rules");
        generalRules.addField("1. Be Respectful:", "Treat all members with respect. Harassment, hate speech, or offensive behavior will not be tolerated. \n Punishment: 10 hrs mute", false);
        generalRules.addField("2. No Spamming:", "Repeating same message/message with same meaning 3 or more than 3 times is considered spam. \n Punishment: 3 hrs mute", false);
        generalRules.addField("3. Racism", "Racism, including any derogatory remarks, slurs, or actions directed at someone based on their race or ethnicity, is strictly forbidden. Our community values diversity, and any form of racial hatred will result in immediate and severe consequences, including potential permanent bans. \n Punishment: One month ban -> One month ban -> Permanent ban", false);
        generalRules.addField("4. Discrimination", "Discrimination based on religion, gender, sexual orientation, nationality, or any other personal characteristic is not allowed. Every player deserves to be treated with respect and fairness. Violations of this rule will be taken seriously and dealt with promptly. \n ", false);
        generalRules.addField("5. Keep Content Appropriate:", "Keep all discussions and shared content appropriate for all ages. No NSFW posts, excessive profanity, or inappropriate memes. \n Punishment: 120 days ban", false);
        generalRules.addField("6. Follow Channel Guidelines:", "Use the appropriate channels for specific topics (e.g., Server suggestions in <#1251522380652740608>). Read the channel descriptions before posting. \n Punishment: Warning -> Warning -> 5 day mute", false);
        generalRules.addField("7. No Advertising:", "Do not advertise other servers, services, or content without permission from the server managers. \n Punishment: 120 days ban", false);
        generalRules.addField("8. Protect Personal Information:", "Do not share your or others' personal information (e.g., addresses, phone numbers) on the server. This includes doxxing. \n Punishment: Permanent Ban", false);
        generalRules.addField("9. Use Appropriate Usernames and Avatars:", "Usernames and avatars must be appropriate and not contain offensive or inappropriate content. If in doubt, ask a staff member. \n Punishment: 10 days ban", false);
        generalRules.addField("10. No Impersonation:", "Do not impersonate other users, staff members, or public figures. This includes using similar usernames or avatars to deceive others. \n Punishment: 20 days ban", false);
        generalRules.addField("11. No Politics or Religion:", "Avoid discussions about politics, religion, or other sensitive topics that may cause disagreements or conflict among members. \n Punishment: 5 days mute", false);
        generalRules.addField("12. Keep Voice Channels Clean:", "No excessive noise, yelling, inappropriate sounds in voice channels, hopping continuously. Be respectful of others using the voice channels. \n Punishment: Warning -> Warning -> 3 days mute", false);
        generalRules.addField("13. Respect Privacy:", "Private messages should remain private. Do not screenshot or share private conversations without the consent of all parties involved. \n Punishment: Decided by the victim and staff based on sensitivity", false);
        generalRules.addField("14. Acknowledge Warnings:", "If you receive a warning from staff, take it seriously. They are not a joke", false);
        generalRules.addField("15. Mute Evading", "Using another account to bypass your mute time is not allowed. \n Punishment: 2 days ban", false);
        generalRules.addField("16. Ban Evading", "Using another account under same or different IP to avoid punishment is completely forbidden 20 days ban", false);
        generalRules.addField("17. Chat Flooding", "Flooding the chat with excessive messages, whether intentional or not, is prohibited. This includes sending multiple lines of text in quick succession, using long strings of characters. \n Punishment: 4 hrs mute", false);
        EmbedBuilder embedBuilder = generalRules;
        return embedBuilder;
    }

    private static EmbedBuilder getMcRules() {
        EmbedBuilder mcRules = new EmbedBuilder();
        mcRules.setDescription("### Minecraft Rules");
        mcRules.addField("18. Griefing", "Griefing, including the destruction or modification of another player\u2019s builds, structures, or property, is strictly prohibited. Respect the hard work and creativity of the players.", false);
        mcRules.addField("19. Stealing", "Taking items, blocks, or resources from another player\u2019s chests, storage, or base without their explicit permission is not allowed", false);
        mcRules.addField("20. Using hacks, modifications", "The use of hacks, cheats, mods, or exploits that provide an unfair advantage over others is forbidden. This includes, but is not limited to, x-ray mods, fly hacks, Kill aura, and auto clicker. Play fair to ensure a balanced and equal experience for all.", false);
        mcRules.addField("21. Be Respectful", "We expect all players to treat each other with kindness and respect. Harassment, bullying, or the use of offensive language is not tolerated. You should be treating others the way you want to be treated. Disputes should be handled maturely or brought to the attention of server staff.", false);
        mcRules.addField("22. Inappropriate builds", "All player-created content, including buildings, signs, maps etc must be appropriate for all. Offensive, explicit, or inappropriate content will be removed, and offending players may face disciplinary action.", false);
        mcRules.addField("23. Use Common Sense", "If an action seems unfair, harmful, or offensive, it likely is. Use your best judgment and consider how your actions may impact others. When in doubt, consult a staff member. Don't try to take action on your own, report and leave that to staff.", false);
        mcRules.addField("24. Reporting Bugs and Exploits", "If you encounter a bug, glitch, or exploit, report it to the server staff immediately. Exploiting these issues for personal gain is strictly prohibited.", false);
        mcRules.addField("25. No Excessive Lag Machines", "Redstone contraptions or other machines that cause significant server lag are prohibited. If your build is causing performance issues, staff may ask you to modify or remove it.", false);
        mcRules.addField("26. No Exploit Discussion", "Discussing or sharing methods to exploit the game or server is strictly prohibited. Any exploit discovered should be reported immediately to staff.", false);
        mcRules.addField("27. Inappropriate Skins or Capes", "Usage of Skins or Capes containing inappropriate material, curses and offensive topics are forbidden.", false);
        mcRules.addField("28. Inappropriate usernames or nicks", "Offensive, inappropriate names are not allowed.", false);
        mcRules.addField("29. Begging for Items or Ranks", "Begging for items, ranks, or other perks is not allowed. Earn items and ranks through legitimate gameplay and server activities.", false);
        mcRules.addField("30. No False Reporting", "Do not make false reports against other players. Misuse of the reporting system can waste staff time and may result in penalties against you.", false);
        mcRules.addField("31. Unauthorized Selling or Trading", "Do not engage in unauthorized real-money transactions or trading of in-game items for real-world currency. All trades should be conducted within the server's guidelines.", false);
        return mcRules;
    }

    private static /* synthetic */ void $$$reportNull$$$0(int n) {
        throw new IllegalStateException(String.format("@NotNull method %s.%s must not return null", "com/dreggcake/src/Commands/Useful/Rules", "getEmbedBuilder"));
    }
}

