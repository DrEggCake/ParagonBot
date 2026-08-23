package com.dreggcake.src.Media;

import com.dreggcake.src.Media.Logic.MediaDAO;
import com.dreggcake.src.Utility.Utilities;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class MediaPermissions {

    private static final List<Role> contentCreators = List.of(
            Utilities.PARTNER_ROLE,
            Utilities.CONTENT_CREATOR_1_ROLE,
            Utilities.CONTENT_CREATOR_2_ROLE,
            Utilities.CONTENT_CREATOR_3_ROLE,
            Utilities.CONTENT_CREATOR_4_ROLE
    );

    public static boolean canUpload(Member target) {
        if (MediaDAO.isBlacklisted(target)) {
            return false;
        }

        return target.getRoles()
                .stream().anyMatch(contentCreators::contains);
    }

    public static boolean canManagePermission(Member staff) {
        return staff.getRoles().contains(Utilities.MEDIA_MANAGER_ROLE) || staff.hasPermission(Permission.ADMINISTRATOR);
    }


}
