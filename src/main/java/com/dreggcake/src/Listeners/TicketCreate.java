package com.dreggcake.src.Listeners;

import com.dreggcake.src.Main;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;

public class TicketCreate extends ListenerAdapter {


    public static void giveSendPerms(TextChannel channel, Member member) {
        PermissionOverride override = channel.getPermissionOverride(member);

        if (override != null) {
            override.delete().queue(v -> {
                channel.upsertPermissionOverride(member)
                        .grant(
                                Permission.VIEW_CHANNEL,
                                Permission.MESSAGE_SEND,
                                Permission.MESSAGE_ATTACH_FILES
                        )
                        .queue();
            });
        } else {
            channel.upsertPermissionOverride(member)
                    .grant(
                            Permission.VIEW_CHANNEL,
                            Permission.MESSAGE_SEND,
                            Permission.MESSAGE_ATTACH_FILES
                    )
                    .queue();
        }
    }


    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        String componentID = e.getComponentId();
        if (componentID.equals("create-ticket")) {
            handleCreateTicket(e);
        } else if (componentID.equals("claim-ticket")) {
            handleClaimTicket(e);
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("new-ticket")) {
            return;
        }
        String option = event.getSelectedOptions().getFirst().getValue();
        TextChannel channel = event.getChannel().asTextChannel();

        switch (option) {
            case "appeal" -> {
                channel.deleteMessageById(event.getMessageId()).queue();
                handleAppealSupport(event);
            }
            case "reportPlayer" -> {
                channel.deleteMessageById(event.getMessageId()).queue();
                handleReportPlayer(event);


            }
            case "reportBug" -> {
                channel.deleteMessageById(event.getMessageId()).queue();
                handleReportBug(event);

            }
            case "storeSupport" -> {
                handleStoreSupport(event);
                channel.deleteMessageById(event.getMessageId()).queue();

            }
            case "general" -> {
                channel.deleteMessageById(event.getMessageId()).queue();
                handleGeneralSupport(event);

            }
            case "passReset" -> {
                handlePasswordReset(event);
                channel.deleteMessageById(event.getMessageId()).queue();
            }
        }
    }

    private void handleAppealSupport(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();


        channel.getManager().setName("appeal-" + event.getMember().getUser().getAsTag()).queue();
        giveSendPerms(channel, member);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Appeal Support");
        embed.setDescription("### Please answer these questions: \n What is your in game name? \n When were you punished? \n Why were you punished? \n Who punished you? ");
        embed.addField("User:", member.getAsMention(), false);
        embed.addField("Claimed by: ", "N/A", false);
        Button button = Button.primary("claim-ticket", "Claim Ticket");
        channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();
        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());

    }

    private void handleReportPlayer(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("report-" + event.getMember().getUser().getAsTag()).queue();
        giveSendPerms(channel, member);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Player Report");
        embed.setDescription("### Please answer these questions: \n What is your in game name? \n Whom do you want to report? \n What is the reason for report? \n\n Please attach any evidence that you have and wait for our staff to review it ");
        embed.addField("User:", member.getAsMention(), false);
        embed.addField("Claimed by: ", "N/A", false);
        Button button = Button.primary("claim-ticket", "Claim Ticket");
        channel.sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(button)).queue();
        channel.sendMessage(member.getAsMention()).queue(msg -> msg.delete().queue());
        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());


    }

    private void handleReportBug(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("bug-" + event.getMember().getUser().getAsTag()).queue();
        giveSendPerms(channel, member);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bug Report");
        embed.setDescription("### Please answer these questions: \n What is your in game name? \n Where did you encounter the bug? \n Describe the bug with as much detail as you can with steps to reproduce it \n Please attach any image, video showcasing the bug for us to understand");
        embed.addField("User:", member.getAsMention(), false);
        embed.addField("Claimed by: ", "N/A", false);
        Button button = Button.primary("claim-ticket", "Claim Ticket");
        channel.sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(button)).queue();
        channel.sendMessage(member.getAsMention()).queue(msg -> msg.delete().queue());
        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());

    }

    private void handleStoreSupport(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("store-" + event.getMember().getUser().getAsTag()).queue();
        giveSendPerms(channel, member);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Purchase Support");
        embed.setDescription("### Please answer these questions: \n What is your in game name? \n What was your purchase? \n When did you purchase?");
        embed.addField("User:", member.getAsMention(), false);
        embed.addField("Claimed by: ", "N/A", false);
        Button button = Button.primary("claim-ticket", "Claim Ticket");
        channel.sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(button)).queue();
        channel.sendMessage(member.getAsMention()).queue(msg -> msg.delete().queue());
        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());

    }

    private void handleGeneralSupport(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("general-" + event.getMember().getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("General Support");
        embed.setDescription("### Please answer these questions: \n What is your in game name? \n What is your issue?");
        embed.addField("User:", member.getAsMention(), false);
        embed.addField("Claimed by: ", "N/A", false);
        Button button = Button.primary("claim-ticket", "Claim Ticket");
        channel.sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(button)).queue();
        channel.sendMessage(member.getAsMention()).queue(msg -> msg.delete().queue());
        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());

    }

    private void handlePasswordReset(StringSelectInteractionEvent event) {

        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("pass-" + event.getMember().getUser().getAsTag()).queue();
        giveSendPerms(channel, member);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Password Reset");
        embed.setDescription("### Please answer the questions below, **if you do not answer every question properly ticket will be closed instantly**: \n- What is your in-game name? \n- What is your ip address? (You can get this on https://whatismyipaddress.com/) \n- When did you last join the server and what gamemode were you in? \n- Do you have any other registered accounts on the server? \n- Names of all the accounts you have registered on the server.");
        embed.addField("User:", member.getAsMention(), false);
        embed.addField("Claimed by: ", "N/A", false);
        Button button = Button.primary("claim-ticket", "Claim Ticket");
        channel.sendMessageEmbeds(embed.build()).setComponents(ActionRow.of(button)).queue();
        channel.sendMessage(member.getAsMention()).queue(msg -> msg.delete().queue());
        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());
    }

    private void handleCreateTicket(ButtonInteractionEvent e) {
        Member member = e.getMember();
        Guild guild = e.getGuild();
        if (member == null || guild == null) return;


        e.deferReply(true).queue();

        // Blacklist check
        if (Utilities.TICKET_BLACKLIST_ROLE != null && member.getRoles().contains(Utilities.TICKET_BLACKLIST_ROLE)) {
            e.getHook().sendMessage("Sorry, but you are __blacklisted__ form creating tickets!").queue();
            return;
        }

        guild.createTextChannel("ticket-" + member.getUser().getName(), Utilities.TICKET_CATEGORY)
                .queue(channel -> {

                    TextChannelManager manager = channel.getManager();

                    // ───────── USER: VIEW ONLY (NO SEND) ─────────
                    manager = manager.putMemberPermissionOverride(
                            member.getIdLong(),
                            EnumSet.of(Permission.VIEW_CHANNEL),
                            EnumSet.of(Permission.MESSAGE_SEND)
                    );

                    // ───────── STAFF & ABOVE: VIEW + SEND ─────────
                    for (Role role : guild.getRoles()) {
                        if(role.equals(Utilities.BOSS_DESIGNER_ROLE) || role.equals(Utilities.BUILDER_ROLE)) continue;
                        if (Utilities.STAFF_ROLE != null && role.getPosition() > Utilities.STAFF_ROLE.getPosition()) {
                            manager = manager.putRolePermissionOverride(
                                    role.getIdLong(),
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND),
                                    EnumSet.noneOf(Permission.class)
                            );
                        }
                    }

                    // ───────── @EVERYONE: CANNOT SEE ─────────
                    manager = manager.putRolePermissionOverride(
                            guild.getPublicRole().getIdLong(),
                            EnumSet.noneOf(Permission.class),
                            EnumSet.of(Permission.VIEW_CHANNEL)
                    );

                    // APPLY ALL PERMISSIONS
                    manager.queue();

                    try {
                        String insert = "INSERT OR IGNORE INTO tickets(ticketid, userid, created_at) VALUES (?, ?, ?)";

                        PreparedStatement stmt = Main.conn.prepareStatement(insert);
                        stmt.setString(1, channel.getId());                 // ticketid
                        stmt.setString(2, member.getId());                  // userid
                        stmt.setLong(3, System.currentTimeMillis());        // created_at

                        stmt.executeUpdate();
                        stmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    Emoji generalEmoji = Emoji.fromCustom("general", Long.parseLong("1455193100338335777"), false);
                    Emoji storeEmoji = Emoji.fromCustom("store", Long.parseLong("1455193095863013488"), false);
                    Emoji bugEmoji = Emoji.fromCustom("bug", Long.parseLong("1455193092604035153"), false);
                    Emoji reportEmoji = Emoji.fromCustom("report", Long.parseLong("1455193089558974681"), false);
                    Emoji appealEmoji = Emoji.fromCustom("appeal", Long.parseLong("1455193081841320110"), false);
                    Emoji passEmoji = Emoji.fromCustom("password", Long.parseLong("1455618498024968192"), false);

                    // ───────── EMBED + MENU ─────────
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.ORANGE)
                            .setTitle("Support Type")
                            .setDescription("Please select the type of support you need.")
                            .addField("User", member.getAsMention(), false);

                    StringSelectMenu menu = StringSelectMenu.create("new-ticket")
                            .setPlaceholder("Select the Support Type!")
                            .addOption("General Support", "general", generalEmoji)
                            .addOption("Appeal Support", "appeal", appealEmoji)
                            .addOption("Report Player", "reportPlayer", reportEmoji)
                            .addOption("Report Bug", "reportBug", bugEmoji)
                            .addOption("Store Support", "storeSupport", storeEmoji)
                            .addOption("Password Reset", "passReset", passEmoji)
                            .setRequiredRange(1, 1)
                            .build();

                    channel.sendMessageEmbeds(embed.build())
                            .setComponents(ActionRow.of(menu))
                            .queue();

                    channel.sendMessage(member.getAsMention())
                            .queue(msg -> msg.delete().queue());

                    e.getHook().sendMessage("Ticket created: " + channel.getAsMention()).queue();
                });

    }

    private void handleClaimTicket(ButtonInteractionEvent e) {
        Member member = e.getMember();

        if (member.getRoles().isEmpty() || !(member.getRoles().contains(Utilities.STAFF_ROLE))) {
            e.reply("Only a staff member can claim ticket").setEphemeral(true).queue();
        } else {
            MessageEmbed embed = e.getMessage().getEmbeds().get(0);
            EmbedBuilder newEmbed = new EmbedBuilder();
            newEmbed.setTitle(embed.getTitle());
            newEmbed.setDescription(embed.getDescription());
            newEmbed.addField("User: ", embed.getFields().get(0).getValue(), false);
            newEmbed.addField("Claimed By:", member.getAsMention(), false);

            e.reply("Ticket claimed by " + member.getAsMention()).queue(msg -> {
                e.getMessage().editMessageEmbeds((newEmbed.build())).queue();
                e.editButton(e.getButton().asDisabled()).queue();
            });
        }
    }

}
