package com.dreggcake.src.Miscellaneous;

import com.dreggcake.src.Utility.Channels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;

/**
 * Daily shoutout for the server's partners, posted in the partner channel.
 */
public class PartnerReminder implements Runnable {

    /* ================= Timings ================= */

    /**
     * Hour of day the reminder fires.
     */
    public static final int REMINDER_HOUR = 18;

    @Override
    public void run() {
        TextChannel channel = Channels.PARTNER_QUEEN;
        if (channel == null) return;

        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("🤝 Check out our Partners!")
                .setDescription(
                        "Give some love to our amazing partner servers and their projects!\n\n" +
                                "Partner highlights coming soon — stay tuned.")
                .setColor(new Color(0x9B59B6))
                .setFooter("Paragon Network Partner Program")
                .build()).queue();
    }
}
