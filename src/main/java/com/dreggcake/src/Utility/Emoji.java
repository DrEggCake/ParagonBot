package com.dreggcake.src.Utility;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;

import java.util.List;

public class Emoji {

    public static ApplicationEmoji CHECK;
    public static ApplicationEmoji CROSS;
    public static ApplicationEmoji LOADING;

    public static void load(JDA jda) {
        List<ApplicationEmoji> emojis =
                jda.retrieveApplicationEmojis().complete();

        for (ApplicationEmoji emoji : emojis) {
            switch (emoji.getName()) {
                case "check" -> CHECK = emoji;
                case "cross" -> CROSS = emoji;
                case "loading" -> LOADING = emoji;
            }
        }

        System.out.println("Loaded " + emojis.size() + " application emojis.");
    }
}
