package com.dreggcake.src.AutoModService.Common;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Set;
import com.dreggcake.src.Utility.Roles;

public class AutoModBypassService {

    // Roles that bypass automod service
    private static final Set<Long> BYPASS_ROLE_IDS = Set.of(
            Roles.STAFF.getIdLong(),
            Roles.PARTNER.getIdLong()
    );

    private AutoModBypassService() {
        // prevent getting instansiated
    }

    public static boolean shouldBypass(Member member) {
        if (member == null)
            return true;

        // Server owner
        if (member.isOwner())
            return true;

        // Administrators
        if (member.hasPermission(Permission.ADMINISTRATOR))
            return true;

        // Whitelisted roles
        for (Role role : member.getRoles()) {
            if (BYPASS_ROLE_IDS.contains(role.getIdLong())) {
                return true;
            }
        }

        return false;
    }
    public static boolean shouldBypassAttachments(Member member) {
        if (member == null)
            return true;

        // Server owner
        if (member.isOwner())
            return true;

        // Administrators
        if (member.hasPermission(Permission.ADMINISTRATOR))
            return true;

        return false;
    }
}
