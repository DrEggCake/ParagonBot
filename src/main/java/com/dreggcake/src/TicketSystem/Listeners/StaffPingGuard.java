package com.dreggcake.src.TicketSystem.Listeners;

import com.dreggcake.src.TicketSystem.TicketCloser;
import com.dreggcake.src.TicketSystem.TicketDAO;
import com.dreggcake.src.Utility.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Warns ticket openers who ping staff members. Warnings live in RAM and
 * the window expires 10 minutes after the first warning; three warnings
 * inside one window close the ticket.
 */
public class StaffPingGuard extends ListenerAdapter {

    private static final int MAX_WARNINGS = 3;
    private static final long WINDOW_TTL = TimeUnit.MINUTES.toMillis(10);

    private record WarningWindow(int count, long firstWarningAt) {
    }

    /**
     * "channelId:userId" -> warning window (RAM cache, expires)
     */
    private static final Map<String, WarningWindow> WARNINGS = new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Member member = event.getMember();
        if (member == null) return;

        long channelId = event.getChannel().getIdLong();
        if (!TicketDAO.isTicket(channelId)) return;

        if (member.getRoles().contains(Roles.STAFF)) return;

        boolean pingsStaff = event.getMessage().getMentions().getMembers().stream()
                .anyMatch(m -> m.getRoles().contains(Roles.STAFF));
        if (!pingsStaff) return;

        if (!(event.getChannel() instanceof TextChannel channel)) return;

        if (event.getMessage().getReferencedMessage() != null)
            return;

        String key = channelId + ":" + member.getId();
        long now = System.currentTimeMillis();

        WarningWindow window = WARNINGS.get(key);
        if (window != null && now - window.firstWarningAt() > WINDOW_TTL) {
            WARNINGS.remove(key);
            window = null;
        }

        int count = (window == null ? 0 : window.count()) + 1;
        WARNINGS.put(key, new WarningWindow(count, window == null ? now : window.firstWarningAt()));

        if (count >= MAX_WARNINGS) {
            WARNINGS.remove(key);
            TicketCloser.close(channel, "AutoClose (repeated staff pings)",
                    member.getAsMention() + " you were warned **" + MAX_WARNINGS +
                            " times** not to ping staff here.\nThe ticket will be deleted in 10 seconds.", 10);
            return;
        }

        event.getMessage().replyEmbeds(new EmbedBuilder()
                .setTitle("Warning " + count + "/" + MAX_WARNINGS)
                .setDescription(member.getAsMention() + ", please do **not** ping staff members in tickets.\n" +
                        "Staff are notified automatically. Warnings reset <t:" +
                        ((window == null ? now : window.firstWarningAt() + WINDOW_TTL) / 1000) + ":R>.")
                .setColor(Color.ORANGE)
                .build()).queue();
    }
}
