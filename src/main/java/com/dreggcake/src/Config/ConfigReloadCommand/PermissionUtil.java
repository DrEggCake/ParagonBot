package com.dreggcake.src.Config.ConfigReloadCommand;

import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionUtil {

    public static boolean canManageConfig(Member member) {
        if (member == null) return false;

        Role base = Utilities.MANAGER_ROLE;
        if (base == null) return false;


        if(member.isOwner()) return true;
        if(member==Utilities.DREGGCAKE) return true;

        return member.getRoles().stream()
                .anyMatch(r -> r.getPosition() >= base.getPosition());
    }
}
