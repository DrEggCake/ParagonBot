package com.dreggcake.src.StaffList;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class StaffRole {

    private final List<Role> universalRole = List.of(
            Utilities.OWNER_ROLE,
            Utilities.DEV_ROLE,
            Utilities.MANAGER_ROLE,
            Utilities.BOSS_DESIGNER_ROLE,
            Utilities.BUILDER_ROLE,
            Utilities.SR_ADMIN_ROLE,
            Utilities.JR_ADMIN_ROLE
    );

    private final List<Role> commonStaff = List.of(
            Utilities.SR_MOD_ROLE,
            Utilities.JR_MOD_ROLE,
            Utilities.SUPPORT_ROLE
    );

    public List<Role> getUniversalRole() {

        return universalRole;
    }

    public List<Role> getCommonStaff() {
        return commonStaff;
    }
}
