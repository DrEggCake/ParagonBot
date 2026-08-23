package com.dreggcake.src.Media;

import com.dreggcake.src.Media.Logic.MediaDAO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import com.dreggcake.src.Utility.Roles;

public class MediaPermissions {

    private static final List<Role> contentCreators = List.of(
            Roles.PARTNER,
            Roles.CONTENT_CREATOR_1,
            Roles.CONTENT_CREATOR_2,
            Roles.CONTENT_CREATOR_3,
            Roles.CONTENT_CREATOR_4
    );

    public static boolean canUpload(Member target) {
        if (MediaDAO.isBlacklisted(target)) {
            return false;
        }

        return target.getRoles()
                .stream().anyMatch(contentCreators::contains);
    }

    public static boolean canManagePermission(Member staff) {
        return staff.getRoles().contains(Roles.MEDIA_MANAGER) || staff.hasPermission(Permission.ADMINISTRATOR);
    }


}
