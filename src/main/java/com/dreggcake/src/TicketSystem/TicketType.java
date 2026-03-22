package com.dreggcake.src.TicketSystem;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum TicketType {
    TICKET_TYPE("ticket-type") {
        @Override
        public void apply(TextChannel channel, Member member) {
        }
    },
    GENERAL("general") {
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(GENERAL.id + "-" + member.getId()).queue();
            channel.getManager().putPermissionOverride(
                    member, EnumSet.of(Permission.MESSAGE_SEND), null
            ).queue();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("General Support");
            embed.setDescription("Please answer the questions below\n");
            embed.appendDescription("- What is your minecraft username?\n");
            embed.appendDescription("- What gamemode do you have issue in?\n");
            embed.appendDescription("- What is your issue?\n");
            embed.setFooter("ParagonMC Tickets");
            embed.addField("Claimed by:", "", false);
            embed.setThumbnail(channel.getGuild().getIconUrl());
            embed.setColor(Color.GREEN);

            Button button = Button.primary(TicketButton.CLAIM.getId(), "Claim Ticket");

            channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();

        }
    },
    APPEAL("appeal") {
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(APPEAL.id + "-" + member.getId()).queue();
            channel.getManager().putPermissionOverride(
                    member, EnumSet.of(Permission.MESSAGE_SEND), null
            ).queue();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Appeal Support");
            embed.setDescription("Please answer the questions below\n");
            embed.appendDescription("- What is your minecraft username?\n");
            embed.appendDescription("- What were you punished for?\n");
            embed.appendDescription("- When were you punished?\n");
            embed.appendDescription("- Which staff member punished you?\n");
            embed.appendDescription("- What gamemode were you punished in?\n");
            embed.setFooter("ParagonMC Tickets");
            embed.addField("Claimed by:", "", false);
            embed.setThumbnail(channel.getGuild().getIconUrl());
            embed.setColor(Color.GREEN);

            Button button = Button.primary(TicketButton.CLAIM.getId(), "Claim Ticket");

            channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();

        }
    },
    REPORT("report") {
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(REPORT.id + "-" + member.getId()).queue();
            channel.getManager().putPermissionOverride(
                    member, EnumSet.of(Permission.MESSAGE_SEND), null
            ).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Report Player");
            embed.setDescription("Please answer the questions below\n");
            embed.appendDescription("- What is your minecraft username?\n");
            embed.appendDescription("- What gamemode do you want to report in?\n");
            embed.appendDescription("- Which player do you want to report?\n");
            embed.appendDescription("- Why do you want to report?\n");
            embed.setFooter("ParagonMC Tickets");
            embed.addField("Claimed by:", "", false);
            embed.setThumbnail(channel.getGuild().getIconUrl());
            embed.setColor(Color.GREEN);

            Button button = Button.primary(TicketButton.CLAIM.getId(), "Claim Ticket");

            channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();

        }
    },
    BUG("bug") {
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(BUG.id + "-" + member.getId()).queue();
            channel.getManager().putPermissionOverride(
                    member, EnumSet.of(Permission.MESSAGE_SEND), null
            ).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Bug Report");
            embed.setDescription("Please answer the questions below\n");
            embed.appendDescription("- What is your minecraft username?\n");
            embed.appendDescription("- What gamemode did you encounter the bug?\n");
            embed.appendDescription("- Describe the bug with as much detail as you can\n");
            embed.appendDescription("- List the steps to reproduce the bug\n");
            embed.setFooter("ParagonMC Tickets");
            embed.addField("Claimed by:", "", false);
            embed.setThumbnail(channel.getGuild().getIconUrl());
            embed.setColor(Color.GREEN);

            Button button = Button.primary(TicketButton.CLAIM.getId(), "Claim Ticket");

            channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();

        }
    },
    STORE("store") {
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(STORE.id + "-" + member.getId()).queue();
            channel.getManager().putPermissionOverride(
                    member, EnumSet.of(Permission.MESSAGE_SEND), null
            ).queue();

            channel.getManager().putPermissionOverride(
                    Utilities.STAFF_ROLE, null, EnumSet.of(Permission.VIEW_CHANNEL)
            ).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Store Support");
            embed.setDescription("Please answer the questions below\n");
            embed.appendDescription("- What is your minecraft username?\n");
            embed.appendDescription("- Have you made a purchase? If yes, when?\n");
            embed.appendDescription("- What is your issue?\n");
            embed.setFooter("ParagonMC Tickets");
            embed.addField("Claimed by:", "", false);
            embed.setThumbnail(channel.getGuild().getIconUrl());
            embed.setColor(Color.GREEN);

            Button button = Button.primary(TicketButton.CLAIM.getId(), "Claim Ticket");

            channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();

        }
    },
    PASSWORD("password") {
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(PASSWORD.id + "-" + member.getId()).queue();
            putPermissions(channel, member);

            channel.getManager().putPermissionOverride(
                    Utilities.STAFF_ROLE, null, EnumSet.of(Permission.VIEW_CHANNEL)
            ).queue();

            channel.getManager().putPermissionOverride(
                    Utilities.SR_ADMIN_ROLE, EnumSet.of(Permission.VIEW_CHANNEL), null
            ).queue();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Password Support");
            embed.setDescription("Please answer the questions below\n");
            embed.appendDescription("- What is your minecraft username?\n");
            embed.appendDescription("- When was the last time you logged in?\n");
            embed.appendDescription("- What gamemode did you play in last time?\n");
            embed.appendDescription("What is your IP-Address? You can get this at https://whatismyipaddress.com/\n");
            embed.appendDescription("\n If you do not answer all the questions ticket will be closed instantly\n");
            embed.setFooter("ParagonMC Tickets");
            embed.addField("Claimed by:", "", false);
            embed.setThumbnail(channel.getGuild().getIconUrl());
            embed.setColor(Color.GREEN);

            Button button = Button.primary(TicketButton.CLAIM.getId(), "Claim Ticket");

            channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(button)).queue();

        }
    };

    private static final Map<String, TicketType> BY_ID = new HashMap<>();

    static {
        for (TicketType b : values()) {
            BY_ID.put(b.id, b);
        }
    }

    private final String id;

    TicketType(String id) {
        this.id = id;
    }

    public static TicketType fromId(String id) {
        return BY_ID.get(id);
    }

    private static void putPermissions(TextChannel channel, Member member) {
        channel.getManager().putPermissionOverride(
                member,
                EnumSet.of(Permission.MESSAGE_SEND),
                null
        ).queue();
    }

    public String getId() {
        return id;
    }

    public abstract void apply(TextChannel channel, Member member);

}
