package com.dreggcake.src.Utility;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

/**
 * All configured roles, resolved once at startup.
 */
public final class Roles {

    // ================== Staff ==================
    public static Role OWNER;
    public static Role DEV;
    public static Role MANAGER;
    public static Role SR_ADMIN;
    public static Role JR_ADMIN;
    public static Role SR_MOD;
    public static Role JR_MOD;
    public static Role SUPPORT;
    public static Role STAFF;
    public static Role BOSS_DESIGNER;
    public static Role BUILDER;
    public static Role MEDIA_MANAGER;

    // ================== Media / Partners ==================
    public static Role PARTNER;
    public static Role CONTENT_CREATOR_1;
    public static Role CONTENT_CREATOR_2;
    public static Role CONTENT_CREATOR_3;
    public static Role CONTENT_CREATOR_4;

    // ================== Punishments ==================
    public static Role TICKET_BLACKLIST;
    public static Role MUTED;

    // ================== Gamemodes ==================
    public static Role SURVIVAL;
    public static Role LIFESTEAL;
    public static Role EARTH;
    public static Role PRISON;
    public static Role COBBLEMON;
    public static Role MINIGAMES;

    private Roles() {
    }

    public static void load(Guild guild) {
        OWNER = ConfigResolver.role(guild, "staffRoleID.ownerRoleID");
        DEV = ConfigResolver.role(guild, "staffRoleID.devRoleID");
        MANAGER = ConfigResolver.role(guild, "staffRoleID.managerRoleID");
        SR_ADMIN = ConfigResolver.role(guild, "staffRoleID.srAdminRoleID");
        JR_ADMIN = ConfigResolver.role(guild, "staffRoleID.jrAdminRoleID");
        SR_MOD = ConfigResolver.role(guild, "staffRoleID.srModRoleID");
        JR_MOD = ConfigResolver.role(guild, "staffRoleID.jrModRoleID");
        SUPPORT = ConfigResolver.role(guild, "staffRoleID.supportRoleID");
        STAFF = ConfigResolver.role(guild, "staffRoleID.staffRoleID");
        BOSS_DESIGNER = ConfigResolver.role(guild, "staffRoleID.bossDesignerRoleID");
        BUILDER = ConfigResolver.role(guild, "staffRoleID.builderRoleID");
        MEDIA_MANAGER = ConfigResolver.role(guild, "staffRoleID.mediaManagerRoleID");

        PARTNER = ConfigResolver.role(guild, "media.partnerRoleID");
        CONTENT_CREATOR_1 = ConfigResolver.role(guild, "media.cc1RoleID");
        CONTENT_CREATOR_2 = ConfigResolver.role(guild, "media.cc2RoleID");
        CONTENT_CREATOR_3 = ConfigResolver.role(guild, "media.cc3RoleID");
        CONTENT_CREATOR_4 = ConfigResolver.role(guild, "media.cc4RoleID");

        TICKET_BLACKLIST = ConfigResolver.role(guild, "punishRoleID.ticketBlacklistRoleID");
        MUTED = ConfigResolver.role(guild, "punishRoleID.mutedRoleID");

        SURVIVAL = ConfigResolver.role(guild, "gamemodeRoleID.survivalRoleID");
        LIFESTEAL = ConfigResolver.role(guild, "gamemodeRoleID.lifestealRoleID");
        EARTH = ConfigResolver.role(guild, "gamemodeRoleID.earthRoleID");
        PRISON = ConfigResolver.role(guild, "gamemodeRoleID.prisonRoleID");
        COBBLEMON = ConfigResolver.role(guild, "gamemodeRoleID.cobblemonRoleID");
        MINIGAMES = ConfigResolver.role(guild, "gamemodeRoleID.minigamesRoleID");
    }
}
