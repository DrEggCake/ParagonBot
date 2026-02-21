package com.dreggcake.src.Listeners;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dreggcake.src.Main.conn;

public class InviteCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        if(member==null) return;
        if(!(member.getRoles().contains(Utilities.STAFF_ROLE) || member.getRoles().stream().anyMatch(role -> role.equals(Utilities.STAFF_ROLE) || role.getPosition()>=Utilities.STAFF_ROLE.getPosition()) )){
            return;
        }

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;

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
            showInvites(event, target.getId(), target);
        } else {
            showInviter(event, target.getId(), target);
        }
    }

    /* ================= USER RESOLVER ================= */

    private Member resolveMember(MessageReceivedEvent event, String[] args) {

        // No argument → self
        if (args.length == 1)
            return event.getMember();

        // Mention
        if (!event.getMessage().getMentions().getMembers().isEmpty())
            return event.getMessage().getMentions().getMembers().getFirst();

        String input = args[1];

        // User ID
        if (input.matches("\\d{17,20}")) {
            return event.getGuild().getMemberById(input);
        }

        // Username (case-insensitive)
        for (Member m : event.getGuild().getMembers()) {
            if (m.getUser().getName().equalsIgnoreCase(input)) {
                return m;
            }
        }

        return null;
    }

    /* ================= COMMAND LOGIC ================= */

    private void showInvites(MessageReceivedEvent event, String inviterId, Member target) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT invited_id FROM invites WHERE inviter_id = ?"
        )) {

            ps.setString(1, inviterId);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder(
                    "📊 **Invites for " + target.getUser().getAsTag() + "**\n\n"
            );

            int count = 0;
            while (rs.next()) {
                sb.append("<@").append(rs.getString("invited_id")).append(">\n");
                count++;
            }

            sb.append("\n**Total:** ").append(count);
            event.getChannel().sendMessage(sb.toString()).queue();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showInviter(MessageReceivedEvent event, String invitedId, Member target) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT inviter_id FROM invites WHERE invited_id = ?"
        )) {

            ps.setString(1, invitedId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                event.getChannel().sendMessage(
                        "👤 **" + target.getUser().getAsTag() +
                                "** was invited by <@" + rs.getString("inviter_id") + ">"
                ).queue();
            } else {
                event.getChannel().sendMessage("❌ No invite data found").queue();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
