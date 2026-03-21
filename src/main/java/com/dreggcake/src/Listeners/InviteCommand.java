package com.dreggcake.src.Listeners;

import com.dreggcake.src.Database.Databases;
import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InviteCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;

        Member member = event.getMember();
        if (member == null) return;

        // Role check (cleaned up)
        boolean hasPerm = member.getRoles().stream()
                .anyMatch(role ->
                        role.equals(Utilities.STAFF_ROLE) ||
                                role.getPosition() >= Utilities.STAFF_ROLE.getPosition()
                );

        if (!hasPerm) return;

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args.length == 0) return;

        String cmd = args[0].toLowerCase();
        if (!cmd.equals("!invites") && !cmd.equals("!invitedby")) return;

        Member target = resolveMember(event, args);

        if (target == null) {
            event.getChannel().sendMessage("❌ User not found").queue();
            return;
        }

        if (cmd.equals("!invites")) {
            showInvites(event, target);
        } else {
            showInviter(event, target);
        }
    }

    /* ================= USER RESOLVER ================= */

    private Member resolveMember(MessageReceivedEvent event, String[] args) {

        if (args.length == 1)
            return event.getMember();

        if (!event.getMessage().getMentions().getMembers().isEmpty())
            return event.getMessage().getMentions().getMembers().get(0);

        String input = args[1];

        if (input.matches("\\d{17,20}")) {
            return event.getGuild().getMemberById(input);
        }

        for (Member m : event.getGuild().getMembers()) {
            if (m.getUser().getName().equalsIgnoreCase(input)) {
                return m;
            }
        }

        return null;
    }

    /* ================= COMMAND LOGIC ================= */

    private void showInvites(MessageReceivedEvent event, Member target) {
        try {
            List<String> invitedIds = Databases.getMainDatabase().query(
                    "SELECT invited_id FROM invites WHERE inviter_id = ?",
                    rs -> {
                        List<String> list = new ArrayList<>();
                        while (rs.next()) {
                            list.add(rs.getString("invited_id"));
                        }
                        return list;
                    },
                    target.getId()
            );

            StringBuilder sb = new StringBuilder(
                    "📊 **Invites for " + target.getUser().getAsTag() + "**\n\n"
            );

            for (String id : invitedIds) {
                sb.append("<@").append(id).append(">\n");
            }

            sb.append("\n**Total:** ").append(invitedIds.size());

            event.getChannel().sendMessage(sb.toString()).queue();

        } catch (SQLException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("❌ Database error").queue();
        }
    }

    private void showInviter(MessageReceivedEvent event, Member target) {
        try {
            String inviterId = Databases.getMainDatabase().query(
                    "SELECT inviter_id FROM invites WHERE invited_id = ?",
                    rs -> rs.next() ? rs.getString("inviter_id") : null,
                    target.getId()
            );

            if (inviterId != null) {
                event.getChannel().sendMessage(
                        "👤 **" + target.getUser().getAsTag() +
                                "** was invited by <@" + inviterId + ">"
                ).queue();
            } else {
                event.getChannel().sendMessage("❌ No invite data found").queue();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("❌ Database error").queue();
        }
    }
}