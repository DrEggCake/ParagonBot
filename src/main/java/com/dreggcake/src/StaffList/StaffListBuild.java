package com.dreggcake.src.StaffList;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;

public class StaffListBuild {

    private final StaffRoles staffRoles = new StaffRoles();
    private final List<Member> staffMembers = new ArrayList<>();

    public List<MessageCreateBuilder> buildList() {
        retrieveStaffMembers();

        List<MessageCreateBuilder> messages = new ArrayList<>();

        messages.add(buildUniversalMessage());
        messages.addAll(buildGamemodeMessages());
        messages.add(buildApplyStaffMessage());

        return messages;
    }

    private MessageCreateBuilder buildApplyStaffMessage() {
        Container container = Container.of(
                ActionRow.of(Button.link("https://docs.google.com/forms/d/e/1FAIpQLSdrt6O0➜xu➜M0t6j7XjCY2QbORAVmViJmN816uUWJXpGA3Bqg/viewform", "Apply for Staff"))
        );
        return new MessageCreateBuilder()
                .useComponentsV2()
                .setComponents(container);
    }

    private MessageCreateBuilder buildUniversalMessage() {

        List<ContainerChildComponent> components = new ArrayList<>();

        components.add(TextDisplay.of("## Paragon Network Staff Team"));

        for (Role role : staffRoles.getUniversalStaffRoles()) {

            List<Member> members = getMembersWithRole(role);

            if (members.isEmpty()) continue;

            StringBuilder section = new StringBuilder();
            section.append("### ").append(role.getAsMention()).append("\n");

            for (Member member : members) {
                section.append("➜ ").append(member.getAsMention()).append("\n");
            }

            components.add(TextDisplay.of(section.toString()));
        }

        return new MessageCreateBuilder()
                .useComponentsV2(true)
                .setComponents(Container.of(components));
    }

    private List<MessageCreateBuilder> buildGamemodeMessages() {

        List<MessageCreateBuilder> messages = new ArrayList<>();

        for (Role gamemode : staffRoles.getGamemodeRoles()) {

            List<ContainerChildComponent> components = new ArrayList<>();

            String title = "## " + gamemode.getName() + " Staff";
            components.add(TextDisplay.of(title));

            boolean hasStaff = false;

            for (Role staffRole : staffRoles.getGamemodeStaffRoles()) {

                List<Member> members = getGamemodeStaff(gamemode, staffRole);

                if (members.isEmpty()) continue;

                hasStaff = true;

                StringBuilder section = new StringBuilder();
                section.append("### ").append(staffRole.getAsMention()).append("\n");

                for (Member member : members) {
                    section.append("➜ ").append(member.getAsMention()).append("\n");
                }

                components.add(TextDisplay.of(section.toString()));
            }

            if (!hasStaff) continue;

            messages.add(
                    new MessageCreateBuilder()
                            .useComponentsV2(true)
                            .setComponents(Container.of(components))
            );
        }

        return messages;
    }

    private void retrieveStaffMembers() {
        for (Member member : Utilities.GUILD.getMembers()) {
            if (member.getRoles().contains(staffRoles.getStaffRole())) {
                staffMembers.add(member);
            }
        }
    }

    private List<Member> getMembersWithRole(Role role) {
        List<Member> result = new ArrayList<>();

        for (Member member : staffMembers) {
            if (member.getRoles().contains(role)) {
                result.add(member);
            }
        }

        return result;

    }

    private List<Member> getGamemodeStaff(Role gamemode, Role staffRole) {
        List<Member> result = new ArrayList<>();

        for (Member member : staffMembers) {

            List<Role> roles = member.getRoles();

            // Must have staff role
            if (!roles.contains(staffRole)) continue;

            // Must NOT be universal (important)
            if (hasAnyRole(member, staffRoles.getUniversalStaffRoles())) continue;

            // Must match gamemode
            if (roles.contains(gamemode)) {
                result.add(member);
            }
        }

        return result;
    }

    private boolean hasAnyRole(Member member, List<Role> rolesToCheck) {
        for (Role role : rolesToCheck) {
            if (member.getRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }
}