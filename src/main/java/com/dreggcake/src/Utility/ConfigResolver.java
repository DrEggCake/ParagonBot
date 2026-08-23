package com.dreggcake.src.Utility;

import com.dreggcake.src.Config.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

final class ConfigResolver {

    private ConfigResolver() {
    }

    static String requireId(String key) {
        String value = Config.getString(key, null);
        if (value == null || value.isBlank()) {
            error(key, "ID is missing or empty in config.yml");
            return null;
        }
        return value;
    }

    static String requireString(String key) {
        String value = Config.getString(key, null);
        if (value == null) {
            error(key, "String value is missing in config.yml");
        }
        return value;
    }

    static Guild guild(net.dv8tion.jda.api.JDA jda, String key) {
        String id = requireId(key);
        if (id == null) return null;

        Guild guild = jda.getGuildById(id);
        if (guild == null) {
            error(key, "Guild not found for ID: " + id);
        }
        return guild;
    }

    static Role role(Guild guild, String key) {
        String id = requireId(key);
        if (id == null || guild == null) return null;

        Role role = guild.getRoleById(id);
        if (role == null) {
            error(key, "Role not found for ID: " + id);
        }
        return role;
    }

    static TextChannel textChannel(Guild guild, String key) {
        String id = requireId(key);
        if (id == null || guild == null) return null;

        TextChannel channel = guild.getTextChannelById(id);
        if (channel == null) {
            error(key, "TextChannel not found for ID: " + id);
        }
        return channel;
    }

    static Category category(Guild guild, String key) {
        String id = requireId(key);
        if (id == null || guild == null) return null;

        Category category = guild.getCategoryById(id);
        if (category == null) {
            error(key, "Category not found for ID: " + id);
        }
        return category;
    }

    static User user(net.dv8tion.jda.api.JDA jda, String key) {
        String id = requireId(key);
        if (id == null) return null;

        User user = jda.getUserById(id);
        if (user == null) {
            error(key, "User not found for ID: " + id);
        }
        return user;
    }

    static void error(String key, String message) {
        System.err.println("[CONFIG ERROR] " + key + " → " + message);
    }
}
