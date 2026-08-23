package com.dreggcake.src.StaffList;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class StaffRoles {

    private final Role staffRole = Utilities.STAFF_ROLE;

    private final List<Role> universalStaffRoles = List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.MEDIA_MANAGER_ROLE,
            Utilities.BUILDER_ROLE,
            Utilities.BOSS_DESIGNER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE
    );


    private final List<Role> gamemodeStaffRoles = List.of(
            Utilities.SR_MOD_ROLE,
            Utilities.JR_MOD_ROLE,
            Utilities.SUPPORT_ROLE
    );


    private final List<Role> gamemodeRoles = List.of(
            Utilities.SURVIVAL_ROLE,
            Utilities.LIFESTEAL_ROLE,
            Utilities.PRISON_ROLE,
            Utilities.MINIGAMES_ROLE,
            Utilities.COBBLEMON_ROLE,
            Utilities.EARTH_ROLE
    );

    public Role getStaffRole() {
        return staffRole;
    }

    public List<Role> getUniversalStaffRoles() {
        return universalStaffRoles;
    }

    public List<Role> getGamemodeStaffRoles() {
        return gamemodeStaffRoles;
    }

    public List<Role> getGamemodeRoles() {
        return gamemodeRoles;
    }

}
