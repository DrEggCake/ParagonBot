package com.dreggcake.src.Listeners;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;

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
        if (!event.getComponentId().equals("new-ticket")) return;

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

    // ───────── HANDLERS (unchanged) ─────────

    private void handleAppealSupport(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("appeal-" + member.getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Appeal Support")
                .setDescription("### Please answer these questions: \n What is your in game name? \n When were you punished? \n Why were you punished? \n Who punished you? ")
                .addField("User:", member.getAsMention(), false)
                .addField("Claimed by: ", "N/A", false);

        channel.sendMessageEmbeds(embed.build())
                .addComponents(ActionRow.of(Button.primary("claim-ticket", "Claim Ticket")))
                .queue();

        channel.sendMessage(Utilities.STAFF_ROLE.getAsMention()).queue(msg -> msg.delete().queue());
    }

    private void handleReportPlayer(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("report-" + member.getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Player Report")
                .setDescription("### Please answer these questions: \n What is your in game name? \n Whom do you want to report? \n What is the reason for report?")
                .addField("User:", member.getAsMention(), false)
                .addField("Claimed by: ", "N/A", false);

        channel.sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(Button.primary("claim-ticket", "Claim Ticket")))
                .queue();
    }

    private void handleReportBug(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("bug-" + member.getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Bug Report")
                .setDescription("### Please describe the bug in detail")
                .addField("User:", member.getAsMention(), false)
                .addField("Claimed by: ", "N/A", false);

        channel.sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(Button.primary("claim-ticket", "Claim Ticket")))
                .queue();
    }

    private void handleStoreSupport(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("store-" + member.getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Purchase Support")
                .setDescription("### Please describe your issue")
                .addField("User:", member.getAsMention(), false)
                .addField("Claimed by: ", "N/A", false);

        channel.sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(Button.primary("claim-ticket", "Claim Ticket")))
                .queue();
    }

    private void handleGeneralSupport(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("general-" + member.getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("General Support")
                .setDescription("### What is your issue?")
                .addField("User:", member.getAsMention(), false)
                .addField("Claimed by: ", "N/A", false);

        channel.sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(Button.primary("claim-ticket", "Claim Ticket")))
                .queue();
    }

    private void handlePasswordReset(StringSelectInteractionEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        channel.getManager().setName("pass-" + member.getUser().getAsTag()).queue();
        giveSendPerms(channel, member);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Password Reset")
                .setDescription("### Answer all questions properly")
                .addField("User:", member.getAsMention(), false)
                .addField("Claimed by: ", "N/A", false);

        channel.sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(Button.primary("claim-ticket", "Claim Ticket")))
                .queue();
    }

    // ───────── MAIN CHANGE HERE ─────────

    private void handleCreateTicket(ButtonInteractionEvent e) {
        Member member = e.getMember();
        Guild guild = e.getGuild();
        if (member == null || guild == null) return;

        e.deferReply(true).queue();

        if (Utilities.TICKET_BLACKLIST_ROLE != null &&
                member.getRoles().contains(Utilities.TICKET_BLACKLIST_ROLE)) {

            e.getHook().sendMessage("You are blacklisted from creating tickets").queue();
            return;
        }

        guild.createTextChannel("ticket-" + member.getUser().getName(), Utilities.TICKET_CATEGORY)
                .queue(channel -> {

                    // ✅ DATABASE INSERT FIXED HERE
                    try {
                        Databases.getMainDatabase().update(
                                "INSERT OR IGNORE INTO tickets(ticketid, userid, created_at) VALUES (?, ?, ?)",
                                channel.getId(),
                                member.getId(),
                                System.currentTimeMillis()
                        );
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.ORANGE)
                            .setTitle("Support Type")
                            .setDescription("Please select the type of support you need.")
                            .addField("User", member.getAsMention(), false);

                    StringSelectMenu menu = StringSelectMenu.create("new-ticket")
                            .setPlaceholder("Select support type")
                            .addOption("General", "general")
                            .addOption("Appeal", "appeal")
                            .addOption("Report", "reportPlayer")
                            .addOption("Bug", "reportBug")
                            .addOption("Store", "storeSupport")
                            .addOption("Password", "passReset")
                            .build();

                    channel.sendMessageEmbeds(embed.build())
                            .setComponents(ActionRow.of(menu))
                            .queue();

                    e.getHook().sendMessage("Ticket created: " + channel.getAsMention()).queue();
                });
    }

    private void handleClaimTicket(ButtonInteractionEvent e) {
        Member member = e.getMember();

        if (!member.getRoles().contains(Utilities.STAFF_ROLE)) {
            e.reply("Only staff can claim tickets").setEphemeral(true).queue();
            return;
        }

        MessageEmbed embed = e.getMessage().getEmbeds().get(0);

        EmbedBuilder newEmbed = new EmbedBuilder()
                .setTitle(embed.getTitle())
                .setDescription(embed.getDescription())
                .addField("User", embed.getFields().get(0).getValue(), false)
                .addField("Claimed By", member.getAsMention(), false);

        e.reply("Ticket claimed").queue(msg -> {
            e.getMessage().editMessageEmbeds(newEmbed.build()).queue();
            e.editButton(e.getButton().asDisabled()).queue();
        });
    }
}