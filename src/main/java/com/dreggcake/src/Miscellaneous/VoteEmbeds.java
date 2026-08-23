package com.dreggcake.src.Miscellaneous;

import com.dreggcake.src.Config.Config;
import com.dreggcake.src.Utility.RuntimeValues;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the shared vote/store reminder embed and its link buttons.
 */
final class VoteEmbeds {

    private VoteEmbeds() {
    }

    static MessageEmbed embed() {
        List<String> voteLinks = voteLinks();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🗳 Support Paragon Network!")
                .setColor(new Color(0x3498DB))
                .setDescription(
                        "Voting takes less than a minute and helps the server grow!\n\n" +
                                "• You can vote on **every site, every day**\n" +
                                "• Rewards are waiting for you in-game\n\n" +
                                "While you are at it, check out our store:\n" + storeLink())
                .setFooter("Thank you for supporting Paragon Network!");

        if (!voteLinks.isEmpty()) {
            StringBuilder sites = new StringBuilder();
            for (int i = 0; i < voteLinks.size(); i++) {
                sites.append("• [Vote site #").append(i + 1).append("](").append(voteLinks.get(i)).append(")\n");
            }
            embed.addField("Vote sites", sites.toString(), false);
        }

        return embed.build();
    }

    static List<Button> buttons() {
        List<Button> buttons = new ArrayList<>();

        List<String> voteLinks = voteLinks();
        for (int i = 0; i < voteLinks.size(); i++) {
            buttons.add(Button.link(voteLinks.get(i), "Vote #" + (i + 1)));
        }

        String store = storeLink();
        if (store != null && !store.isBlank()) {
            buttons.add(Button.link(store, "🛒 Store"));
        }
        return buttons;
    }

    /**
     * Buttons chunked into action rows of five (Discord limit).
     */
    static List<ActionRow> rows() {
        List<Button> buttons = buttons();
        List<ActionRow> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 5) {
            rows.add(ActionRow.of(buttons.subList(i, Math.min(i + 5, buttons.size()))));
        }
        return rows;
    }

    private static List<String> voteLinks() {
        List<String> links = Config.getStringList("messages.voteLinks");
        return links == null ? List.of() : links;
    }

    private static String storeLink() {
        String store = RuntimeValues.STORE;
        return store == null || store.isBlank()
                ? Config.getString("messages.store", "")
                : store;
    }
}
