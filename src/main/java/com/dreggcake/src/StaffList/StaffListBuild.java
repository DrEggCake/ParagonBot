package com.dreggcake.src.StaffList;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.dreggcake.src.Utility.GuildContext;

public class StaffListBuild {

    private final StaffRoles staffRoles = new StaffRoles();
    private final List<Member> staffMembers = new ArrayList<>();
    private final List<Member> usedMember = new ArrayList<>();

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
                TextDisplay.of("-# Interested in becoming part of our Staff Team? Click the button below to apply"),
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Button.link("https://forms.gle/kLN4kk4oYATqjhae7", "Apply for Staff"))
        );
        return new MessageCreateBuilder()
                .useComponentsV2()
                .setComponents(container);
    }

    private MessageCreateBuilder buildUniversalMessage() {

        List<ContainerChildComponent> components = new ArrayList<>();

        components.add(TextDisplay.of("# Paragon Network Staff Team"));
        components.add(Separator.createDivider(Separator.Spacing.LARGE));

        for (Role role : staffRoles.getUniversalStaffRoles()) {

            List<Member> members = getMembersWithRole(role);

            if (members.isEmpty()) continue;

            StringBuilder section = new StringBuilder();
            section.append("### ").append(role.getAsMention()).append("\n");

            for (Member member : members) {
                if (usedMember.contains(member)) continue;
                section.append("➜ ").append(member.getAsMention()).append("\n");
                usedMember.add(member);
            }

            components.add(TextDisplay.of(section + "\n"));
        }
        components.add(Separator.createDivider(Separator.Spacing.SMALL));

        return new MessageCreateBuilder()
                .useComponentsV2(true)
                .setComponents(Container.of(components).withAccentColor(Color.ORANGE));
    }

    private List<MessageCreateBuilder> buildGamemodeMessages() {

        List<MessageCreateBuilder> messages = new ArrayList<>();

        for (Role gamemode : staffRoles.getGamemodeRoles()) {

            List<ContainerChildComponent> components = new ArrayList<>();

            String title = "## " + gamemode.getName();
            components.add(TextDisplay.of(title));
            components.add(Separator.createDivider(Separator.Spacing.SMALL));

            boolean hasStaff = false;

            for (Role staffRole : staffRoles.getGamemodeStaffRoles()) {

                List<Member> members = getGamemodeStaff(gamemode, staffRole);

                if (members.isEmpty()) continue;

                hasStaff = true;

                StringBuilder section = new StringBuilder();
                section.append("### ").append(staffRole.getAsMention()).append("\n");

                for (Member member : members) {
                    if (usedMember.contains(member)) continue;
                    section.append("➜ ").append(member.getAsMention()).append("\n");
                    usedMember.add(member);
                }

                components.add(TextDisplay.of(section + "\n"));
            }

            if (!hasStaff) continue;

            messages.add(
                    new MessageCreateBuilder()
                            .useComponentsV2(true)
                            .setComponents(Container.of(components).withAccentColor(Color.BLUE))
            );
        }

        return messages;
    }

    private void retrieveStaffMembers() {
        for (Member member : GuildContext.GUILD.getMembers()) {
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

            if (!roles.contains(staffRole)) continue;

            if (hasAnyRole(member, staffRoles.getUniversalStaffRoles())) continue;

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