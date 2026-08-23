package com.dreggcake.src.StaffList;

import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import com.dreggcake.src.Utility.Roles;

public class StaffRoles {

    private final Role staffRole = Roles.STAFF;

    private final List<Role> universalStaffRoles = List.of(
            Roles.OWNER,
            Roles.DEV,
            Roles.MANAGER,
            Roles.MEDIA_MANAGER,
            Roles.BUILDER,
            Roles.BOSS_DESIGNER,
            Roles.SR_ADMIN,
            Roles.JR_ADMIN
    );


    private final List<Role> gamemodeStaffRoles = List.of(
            Roles.SR_MOD,
            Roles.JR_MOD,
            Roles.SUPPORT
    );


    private final List<Role> gamemodeRoles = List.of(
            Roles.SURVIVAL,
            Roles.LIFESTEAL,
            Roles.PRISON,
            Roles.MINIGAMES,
            Roles.COBBLEMON,
            Roles.EARTH
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
