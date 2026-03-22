package com.dreggcake.src.TicketSystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.w3c.dom.Text;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum TicketType {
    TICKET_TYPE("ticket-type") {
        @Override
        public void apply(TextChannel channel, Member member) {}
    },
    GENERAL("general"){
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(GENERAL.id+ member.getId()).queue();
            putPermissions(channel, member);


        }
    },
    APPEAL("appeal"){
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(APPEAL.id+ member.getId()).queue();
            putPermissions(channel, member);
        }
    },
    REPORT("report"){
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(REPORT.id+ member.getId()).queue();
            putPermissions(channel, member);
        }
    },
    BUG("bug"){
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(BUG.id+ member.getId()).queue();
            putPermissions(channel, member);
        }
    },
    STORE("store"){
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(STORE.id+ member.getId()).queue();
            putPermissions(channel, member);
        }
    },
    PASSWORD("password"){
        @Override
        public void apply(TextChannel channel, Member member) {
            channel.getManager().setName(PASSWORD.id+ member.getId()).queue();
            putPermissions(channel, member);
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

    public String getId() {
        return id;
    }

    public abstract void apply(TextChannel channel, Member member);

    private static void putPermissions(TextChannel channel, Member member){
        channel.getManager().putPermissionOverride(
                member,
                EnumSet.of(Permission.MESSAGE_SEND),
                null
        ).queue();
    }

}
