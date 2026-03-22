package com.dreggcake.src.TicketSystem;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class TicketPermissions {

    public static boolean canSetup(Member member){
        return member.getRoles().contains(Utilities.MANAGER_ROLE) || member.hasPermission(Permission.ADMINISTRATOR);
    }

    public static boolean isBlacklisted(Member member){
        try {
            return com.dreggcake.src.Database.Databases.getMainDatabase().query(
                    "SELECT 1 FROM blacklistTicket WHERE userid = ? LIMIT 1",
                    rs -> rs.next(),
                    member.getId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isStaff(Member member){
        return member.getRoles().contains(Utilities.STAFF_ROLE) || member.hasPermission(Permission.ADMINISTRATOR);
    }
}
