package com.dreggcake.src.Config.ConfigReloadCommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import com.dreggcake.src.Utility.GuildContext;
import com.dreggcake.src.Utility.Roles;

public class PermissionUtil {

    public static boolean canManageConfig(Member member) {
        if (member == null) return false;

        Role base = Roles.MANAGER;
        if (base == null) return false;


        if(member.isOwner()) return true;
        if(member==GuildContext.DREGGCAKE) return true;

        return member.getRoles().stream()
                .anyMatch(r -> r.getPosition() >= base.getPosition());
    }
}
