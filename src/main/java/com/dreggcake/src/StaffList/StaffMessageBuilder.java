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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaffMessageBuilder {

    private final StaffRole staffRoles = new StaffRole();

    /**
     * Build all staff messages as separate containers:
     * - First message = universal staff
     * - Each gamemode staff with members = separate message container
     * - Apply for Staff button = separate message container
     */
    public List<Container> buildAllMessages(Guild guild) {
        List<Container> messages = new ArrayList<>();

        Set<Member> addedMembers = new HashSet<>();

        // --- Universal Staff ---
        List<ContainerChildComponent> universalComponents = buildUniversalStaff(guild, addedMembers);
        if (!universalComponents.isEmpty()) {
            universalComponents.add(Separator.createDivider(Separator.Spacing.LARGE));
            messages.add(Container.of(universalComponents));
        }

        // --- Gamemode Staff ---
        addGamemodeContainer(messages, buildGamemodeStaff(guild, "# **Survival Staff**", Utilities.SURVIVAL_ROLE, addedMembers));
        addGamemodeContainer(messages, buildGamemodeStaff(guild, "# **Lifesteal Staff**", Utilities.LIFESTEAL_ROLE, addedMembers));
        addGamemodeContainer(messages, buildGamemodeStaff(guild, "# **Earth Staff**", Utilities.EARTH_ROLE, addedMembers));
        addGamemodeContainer(messages, buildGamemodeStaff(guild, "# **Prison Staff**", Utilities.PRISON_ROLE, addedMembers));
        addGamemodeContainer(messages, buildGamemodeStaff(guild, "# **Cobblemon Staff**", Utilities.COBBLEMON_ROLE, addedMembers));
        addGamemodeContainer(messages, buildGamemodeStaff(guild, "# **Minigames Staff**", Utilities.MINIGAMES_ROLE, addedMembers));

        // --- Apply for Staff Button in a separate message ---
        List<ContainerChildComponent> buttonComponents = new ArrayList<>();
        buttonComponents.add(ActionRow.of(
                Button.link(
                        "https://docs.google.com/forms/d/e/1FAIpQLSdrt6O0-xu-M0t6j7XjCY2QbORAVmViJmN816uUWJXpGA3Bqg/viewform",
                        "Apply for Staff"
                )
        ));
        messages.add(Container.of(buttonComponents));

        return messages;
    }

    /* ---------------- UNIVERSAL ---------------- */
    private List<ContainerChildComponent> buildUniversalStaff(Guild guild, Set<Member> addedMembers) {
        List<ContainerChildComponent> components = new ArrayList<>();
        components.add(TextDisplay.of("# **Paragon Network Staff List**"));
        components.add(Separator.createDivider(Separator.Spacing.LARGE));

        for (Role role : staffRoles.getUniversalRole()) {
            List<Member> members = new ArrayList<>(guild.getMembersWithRoles(role));
            members.removeIf(addedMembers::contains);

            if (members.isEmpty()) continue;

            components.add(TextDisplay.of("### " + role.getAsMention() + "\n"));

            StringBuilder sb = new StringBuilder();
            for (Member m : members) {
                sb.append("➜ ").append(m.getAsMention()).append("\n");
                addedMembers.add(m);
            }

            components.add(TextDisplay.of(sb.toString()));
        }

        return components;
    }

    /* ---------------- GAMEMODE ---------------- */
    private List<ContainerChildComponent> buildGamemodeStaff(
            Guild guild,
            String title,
            Role gamemodeRole,
            Set<Member> addedMembers
    ) {
        List<ContainerChildComponent> components = new ArrayList<>();
        components.add(TextDisplay.of(title));

        boolean hasMembers = false;

        for (Role staffRole : staffRoles.getCommonStaff()) {
            List<Member> members = guild.getMembersWithRoles(staffRole).stream()
                    .filter(m -> m.getRoles().contains(gamemodeRole))
                    .filter(m -> !addedMembers.contains(m))
                    .toList();

            if (members.isEmpty()) continue;
            hasMembers = true;

            StringBuilder sb = new StringBuilder();
            for (Member m : members) {
                sb.append("➜ ").append(m.getAsMention()).append("\n");
                addedMembers.add(m);
            }

            components.add(TextDisplay.of("### " + staffRole.getAsMention()));
            components.add(TextDisplay.of(sb.toString()));
        }

        return hasMembers ? components : new ArrayList<>();
    }

    private void addGamemodeContainer(List<Container> messages, List<ContainerChildComponent> components) {
        if (!components.isEmpty()) {
            messages.add(Container.of(components));
        }
    }
}