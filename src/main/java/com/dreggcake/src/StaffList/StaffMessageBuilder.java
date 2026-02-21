package com.dreggcake.src.StaffList;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

public class StaffMessageBuilder {

    private final StaffRole staffRoles = new StaffRole();

    public Container build(Guild guild) {
        List<ContainerChildComponent> components = new ArrayList<>();

        components.addAll(buildUniversalStaff(guild));
        components.add(Separator.createInvisible(Separator.Spacing.SMALL));
        components.addAll(buildGamemodeStaff(guild, "## **Survival Staff**", Utilities.SURVIVAL_ROLE));
        components.add(Separator.createInvisible(Separator.Spacing.SMALL));
        components.addAll(buildGamemodeStaff(guild, "## **Lifesteal Staff**", Utilities.LIFESTEAL_ROLE));
        components.add(Separator.createDivider(Separator.Spacing.LARGE));
        components.add(
                ActionRow.of(
                        Button.link("https://docs.google.com/forms/d/e/1FAIpQLSdrt6O0-xu-M0t6j7XjCY2QbORAVmViJmN816uUWJXpGA3Bqg/viewform", "Apply for Staff")
                )
        );
        Container container = Container.of(
                components
        );

        return container;
    }

    /* ---------------- UNIVERSAL ---------------- */

    private List<ContainerChildComponent> buildUniversalStaff(Guild guild) {
        List<ContainerChildComponent> components = new ArrayList<>();

        components.add(TextDisplay.of("# **Paragon Network Staff List**"));
        components.add(Separator.createDivider(Separator.Spacing.LARGE));

        List<Member> usedMembers = new ArrayList<>();

        for (Role role : staffRoles.getUniversalRole()) {
            List<Member> members = new ArrayList<>(guild.getMembersWithRoles(role));
            members.removeIf(usedMembers::contains);

            if (members.isEmpty()) continue;

            // Add role header + newline
            components.add(TextDisplay.of("### " + role.getAsMention() + "\n"));

            StringBuilder sb = new StringBuilder();
            for (Member m : members) {
                sb.append("➜ ").append(m.getAsMention()).append("\n");
                usedMembers.add(m);
            }

            components.add(TextDisplay.of(sb.toString()));
        }

        return components;
    }




    /* ---------------- GAMEMODE ---------------- */

    private List<ContainerChildComponent> buildGamemodeStaff(
            Guild guild,
            String title,
            Role gamemodeRole
    ) {
        List<ContainerChildComponent> components = new ArrayList<>();

        components.add(TextDisplay.of(title));

        List<Member> usedMembers = new ArrayList<>();

        for (Role staffRole : staffRoles.getCommonStaff()) {
            List<Member> members = guild.getMembersWithRoles(staffRole).stream()
                    .filter(m -> m.getRoles().contains(gamemodeRole))
                    .filter(m -> !usedMembers.contains(m))
                    .toList();

            if (members.isEmpty()) continue;

            StringBuilder sb = new StringBuilder();
            for (Member m : members) {
                sb.append("➜ ").append(m.getAsMention()).append("\n");
                usedMembers.add(m);
            }

            components.add(TextDisplay.of("### " + staffRole.getAsMention()));
            components.add(TextDisplay.of(sb.toString()));
        }

        return components;
    }

}
