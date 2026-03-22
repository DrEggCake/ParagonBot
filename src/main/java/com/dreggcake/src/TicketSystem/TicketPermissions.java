package com.dreggcake.src.TicketSystem;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class TicketPermissions {

    public static boolean canSetup(Member member){
        return member.getRoles().contains(Utilities.MANAGER_ROLE) || member.hasPermission(Permission.ADMINISTRATOR);
    }

    public static boolean isBlacklisted(Member member){
        return false;
    }

    public static boolean isStaff(Member member){
        return member.getRoles().contains(Utilities.STAFF_ROLE) || member.hasPermission(Permission.ADMINISTRATOR);
    }
}
