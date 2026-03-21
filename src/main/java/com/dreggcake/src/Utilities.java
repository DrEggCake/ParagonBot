package com.dreggcake.src;

import com.dreggcake.src.Config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Utilities {

    // ================== Guild ==================
    public static Guild GUILD;

    // ================== Roles ==================
    public static Role OWNER_ROLE;
    public static Role DEV_ROLE;
    public static Role MANAGER_ROLE;
    public static Role SR_ADMIN_ROLE;
    public static Role JR_ADMIN_ROLE;
    public static Role SR_MOD_ROLE;
    public static Role JR_MOD_ROLE;
    public static Role SUPPORT_ROLE;
    public static Role STAFF_ROLE;
    public static Role BOSS_DESIGNER_ROLE;
    public static Role BUILDER_ROLE;
    public static Role MEDIA_MANAGER_ROLE;

    public static Role PARTNER_ROLE;
    public static Role CONTENT_CREATOR_1_ROLE;
    public static Role CONTENT_CREATOR_2_ROLE;
    public static Role CONTENT_CREATOR_3_ROLE;
    public static Role CONTENT_CREATOR_4_ROLE;

    public static Role TICKET_BLACKLIST_ROLE;
    public static Role MUTED_ROLE;
    public static Role MEDIA_BACKLIST_ROLE;

    public static Role SURVIVAL_ROLE;
    public static Role LIFESTEAL_ROLE;
    public static Role EARTH_ROLE;
    public static Role PRISON_ROLE;
    public static Role COBBLEMON_ROLE;
    public static Role MINIGAMES_ROLE;

    // ================== Channels ==================
    public static TextChannel PUNISHMENT_LOG_CHANNEL_ID;
    public static TextChannel MEMBER_LOG_CHANNEL_ID;
    public static TextChannel INVITE_LOG_CHANNEL;
    public static TextChannel TICKET_LOG_CHANNEL;
    public static TextChannel TICKET_CHANNEL;
    public static TextChannel WELCOME_CHANNEL;
    public static TextChannel COUNTING_CHANNEL;
    public static TextChannel SPAM_CHANNEL;
    public static TextChannel STAFF_LIST_CHANNEL;
    public static TextChannel SUPPORT_CHANNEL;
    public static TextChannel DO_NOT_DELETE_CHANNEL;
    public static TextChannel MESSAGES_LOG_CHANNEL;
    public static TextChannel MEDIA_LOG_CHANNEL;

    public static TextChannel MEDIA_UPLOAD_CHANNEL;

    // ================== Categories ==================
    public static Category TICKET_CATEGORY;
    public static Category STAFF_LOGS;

    // ================== Users ==================
    public static User DREGGCAKE;

    // ================== Runtime Messages ==================
    public static String IP;
    public static String STORE;

    /**
     * LOAD DISCORD OBJECTS (only needed at startup)
     */
    public static void init(JDA jda) {
        System.out.println("========== Initializing Discord Objects ==========");

        GUILD = getGuild(jda, "Guild.guildID");
        if (GUILD == null) {
            System.err.println("[FATAL] Guild is null. Bot cannot function.");
            return;
        }

        OWNER_ROLE = getRole("staffRoleID.ownerRoleID");
        DEV_ROLE = getRole("staffRoleID.devRoleID");
        MANAGER_ROLE = getRole("staffRoleID.managerRoleID");
        SR_ADMIN_ROLE = getRole("staffRoleID.srAdminRoleID");
        JR_ADMIN_ROLE = getRole("staffRoleID.jrAdminRoleID");
        SR_MOD_ROLE = getRole("staffRoleID.srModRoleID");
        JR_MOD_ROLE = getRole("staffRoleID.jrModRoleID");
        SUPPORT_ROLE = getRole("staffRoleID.supportRoleID");
        STAFF_ROLE = getRole("staffRoleID.staffRoleID");
        BOSS_DESIGNER_ROLE = getRole("staffRoleID.bossDesignerRoleID");
        BUILDER_ROLE = getRole("staffRoleID.builderRoleID");
        MEDIA_MANAGER_ROLE = getRole("staffRoleID.mediaManagerRoleID");

        PARTNER_ROLE = getRole("media.partnerRoleID");
        CONTENT_CREATOR_1_ROLE = getRole("media.cc1RoleID");
        CONTENT_CREATOR_2_ROLE = getRole("media.cc2RoleID");
        CONTENT_CREATOR_3_ROLE = getRole("media.cc3RoleID");
        CONTENT_CREATOR_4_ROLE = getRole("media.cc4RoleID");

        TICKET_BLACKLIST_ROLE = getRole("punishRoleID.ticketBlacklistRoleID");
        MUTED_ROLE = getRole("punishRoleID.mutedRoleID");

        SURVIVAL_ROLE = getRole("gamemodeRoleID.survivalRoleID");
        LIFESTEAL_ROLE = getRole("gamemodeRoleID.lifestealRoleID");
        EARTH_ROLE = getRole("gamemodeRoleID.earthRoleID");
        PRISON_ROLE = getRole("gamemodeRoleID.prisonRoleID");
        COBBLEMON_ROLE = getRole("gamemodeRoleID.cobblemonRoleID");
        MINIGAMES_ROLE = getRole("gamemodeRoleID.minigamesRoleID");

        PUNISHMENT_LOG_CHANNEL_ID = getTextChannel("ChannelsID.punishmentLogID");
        MEMBER_LOG_CHANNEL_ID = getTextChannel("ChannelsID.memberLogID");
        MESSAGES_LOG_CHANNEL = getTextChannel("ChannelsID.messagesLogID");
        INVITE_LOG_CHANNEL = getTextChannel("ChannelsID.inviteLogID");
        TICKET_LOG_CHANNEL = getTextChannel("ChannelsID.ticketLogID");
        TICKET_CHANNEL = getTextChannel("ChannelsID.ticketID");
        WELCOME_CHANNEL = getTextChannel("ChannelsID.welcomeID");
        COUNTING_CHANNEL = getTextChannel("autoModBypassChannelsID.countingID");
        SPAM_CHANNEL = getTextChannel("autoModBypassChannelsID.spammingID");
        STAFF_LIST_CHANNEL = getTextChannel("ChannelsID.staffListChannelID");
        SUPPORT_CHANNEL = getTextChannel("ChannelsID.supportChannelID");
        DO_NOT_DELETE_CHANNEL = getTextChannel("ChannelsID.doNotDeleteID");
        MEDIA_LOG_CHANNEL = getTextChannel("ChannelsID.mediaLogID");

        MEDIA_UPLOAD_CHANNEL = getTextChannel("media.channels.mediaUploadID");

        TICKET_CATEGORY = getCategory("CategoryID.ticketCategoryID");
        STAFF_LOGS = getCategory("CategoryID.staffLogsId");

        DREGGCAKE = getUser(jda, "dreggcake.id");

        System.out.println("========== Discord Objects Loaded ==========");
    }

    /**
     * LOAD RUNTIME VALUES (call on startup AND reload)
     */
    public static void loadRuntimeValues() {
        System.out.println("========== Loading Runtime Config Values ==========");
        IP = getString("messages.ip");
        STORE = getString("messages.store");
        System.out.println("========== Loading Runtime Config Values Finished ==========");
    }

    // ============================================================
    // ================== Helper Methods ==========================
    // ============================================================

    private static Guild getGuild(JDA jda, String key) {
        String id = getId(key);
        if (id == null) return null;

        Guild guild = jda.getGuildById(id);
        if (guild == null) {
            error(key, "Guild not found for ID: " + id);
        }
        return guild;
    }

    private static Role getRole(String key) {
        String id = getId(key);
        if (id == null || GUILD == null) return null;

        Role role = GUILD.getRoleById(id);
        if (role == null) {
            error(key, "Role not found for ID: " + id);
        }
        return role;
    }

    private static TextChannel getTextChannel(String key) {
        String id = getId(key);
        if (id == null || GUILD == null) return null;

        TextChannel channel = GUILD.getTextChannelById(id);
        if (channel == null) {
            error(key, "TextChannel not found for ID: " + id);
        }
        return channel;
    }

    private static Category getCategory(String key) {
        String id = getId(key);
        if (id == null || GUILD == null) return null;

        Category category = GUILD.getCategoryById(id);
        if (category == null) {
            error(key, "Category not found for ID: " + id);
        }
        return category;
    }

    private static User getUser(JDA jda, String key) {
        String id = getId(key);
        if (id == null) return null;

        User user = jda.getUserById(id);
        if (user == null) {
            error(key, "User not found for ID: " + id);
        }
        return user;
    }

    private static String getId(String key) {
        String value = Config.getString(key, null);
        if (value == null || value.isBlank()) {
            error(key, "ID is missing or empty in config.yml");
            return null;
        }
        return value;
    }

    private static String getString(String key) {
        String value = Config.getString(key, null);

        if (value == null) {
            error(key, "String value is missing in config.yml");
            return null;
        }
        return value;
    }

    private static void error(String key, String message) {
        System.err.println("[CONFIG ERROR] " + key + " → " + message);
    }
}
