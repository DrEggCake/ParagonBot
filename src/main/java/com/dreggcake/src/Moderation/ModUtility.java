package com.dreggcake.src.Moderation;

import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import com.dreggcake.src.Utility.Roles;

public class ModUtility{

    private static final List<Role> muteModStaff = List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD,
            Roles.JR_MOD,
            Roles.SUPPORT
    );

    private static final List<Role> banModStaff = List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD,
            Roles.JR_MOD
    );
    private static final List<Role> blacklistTicketStaff= List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );
    private static final List<Role> changeBanStaff= List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );
    private static final List<Role> unmuteModStaff= List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );
    private static final List<Role> unbanModStaff= List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );
    private static final List<Role> unblacklistTicketStaff= List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );
    private static final List<Role> renameTicketStaff= List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN,
            Roles.SR_MOD
    );

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
}
