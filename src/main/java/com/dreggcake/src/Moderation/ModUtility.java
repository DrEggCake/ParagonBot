package com.dreggcake.src.Moderation;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class ModUtility{

    private static final List<Role> muteModStaff = List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE,
            Utilities.JR_MOD_ROLE,
            Utilities.SUPPORT_ROLE
    );

    private static final List<Role> banModStaff = List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE,
            Utilities.JR_MOD_ROLE
    );
    private static final List<Role> blacklistTicketStaff= List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE
    );
    private static final List<Role> changeBanStaff= List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE
    );
    private static final List<Role> unmuteModStaff= List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE
    );
    private static final List<Role> unbanModStaff= List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE
    );
    private static final List<Role> unblacklistTicketStaff= List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE,
            Utilities.SR_MOD_ROLE
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
}
