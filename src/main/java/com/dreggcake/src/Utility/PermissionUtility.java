package com.dreggcake.src.Utility;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central authority for "who is allowed to do what".
 * All staff ladders are derived from one ordered definition.
 */
public final class PermissionUtility {

    // ================== Staff ladder ==================

    /** Owner, Dev, Manager, Sr Admin, Jr Admin, Sr Mod */
    private static final List<Role> SR_MOD_AND_ABOVE = List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );

    private static final List<Role> muteModStaff = plus(SR_MOD_AND_ABOVE, Roles.JR_MOD, Roles.SUPPORT);
    private static final List<Role> banModStaff = plus(SR_MOD_AND_ABOVE, Roles.JR_MOD);
    private static final List<Role> blacklistTicketStaff = SR_MOD_AND_ABOVE;
    private static final List<Role> changeBanStaff = SR_MOD_AND_ABOVE;
    private static final List<Role> unmuteModStaff = SR_MOD_AND_ABOVE;
    private static final List<Role> unbanModStaff = SR_MOD_AND_ABOVE;
    private static final List<Role> unblacklistTicketStaff = SR_MOD_AND_ABOVE;
    private static final List<Role> renameTicketStaff = SR_MOD_AND_ABOVE;

    private PermissionUtility() {
    }

    public static boolean canManageConfig(Member member) {
        if (member == null) return false;
        if (member.isOwner()) return true;
        if (GuildContext.DREGGCAKE != null && member.getId().equals(GuildContext.DREGGCAKE.getId())) return true;

        Role base = Roles.MANAGER;
        if (base == null) return false;

        return member.getRoles().stream()
                .anyMatch(r -> r.getPosition() >= base.getPosition());
    }

    public static List<Role> getMuteModStaff() {
        return muteModStaff;
    }

    public static List<Role> getBanModStaff() {
        return banModStaff;
    }

    public static List<Role> getBlacklistTicketStaff() {
        return blacklistTicketStaff;
    }

    public static List<Role> getChangeBanStaff() {
        return changeBanStaff;
    }

    public static List<Role> getUnbanModStaff() {
        return unbanModStaff;
    }

    public static List<Role> getUnmuteModStaff() {
        return unmuteModStaff;
    }

    public static List<Role> getUnblacklistTicketStaff() {
        return unblacklistTicketStaff;
    }

    public static List<Role> getRenameTicketStaff() {
        return renameTicketStaff;
    }

    private static List<Role> plus(List<Role> base, Role... extra) {
        List<Role> all = new ArrayList<>(base);
        Collections.addAll(all, extra);
        return List.copyOf(all);
    }
}
