package com.dreggcake.src.Utility;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

/**
 * Holds the primary guild the bot operates on and well-known users.
 */
public final class GuildContext {

    public static JDA JDA;

    public static Guild GUILD;

    public static User DREGGCAKE;

    private GuildContext() {
    }

    public static void init(JDA jda) {
        System.out.println("========== Initializing Discord Objects ==========");

        JDA = jda;
        GUILD = ConfigResolver.guild(jda, "Guild.guildID");
        if (GUILD == null) {
            System.err.println("[FATAL] Guild is null. Bot cannot function.");
            return;
        }

        DREGGCAKE = ConfigResolver.user(jda, "dreggcake.id");

        System.out.println("========== Discord Objects Loaded ==========");
    }
}
