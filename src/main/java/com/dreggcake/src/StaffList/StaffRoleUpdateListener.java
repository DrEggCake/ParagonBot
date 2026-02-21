package com.dreggcake.src.StaffList;

import com.dreggcake.src.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StaffRoleUpdateListener extends ListenerAdapter {

    private static final int REFRESH_INTERVAL_HOURS = 6;
    private final Set<Long> trackedRoleIds = new HashSet<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Guild guild;

    /* -------------------- INIT -------------------- */
    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(Utilities.GUILD.getId());
        if (guild == null) return;

        refreshTrackedRoles();

        // Periodic refresh in case a role change is missed
        scheduler.scheduleAtFixedRate(() -> {
            if (guild != null) refreshStaffMessage();
        }, 0, REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
    }


    /* -------------------- HELPER METHODS -------------------- */
    private void refreshTrackedRoles() {
        trackedRoleIds.clear();

        List<Role> roles = List.of(
                Utilities.OWNER_ROLE,
                Utilities.DEV_ROLE,
                Utilities.MANAGER_ROLE,
                Utilities.BOSS_DESIGNER_ROLE,
                Utilities.BUILDER_ROLE,
                Utilities.SR_ADMIN_ROLE,
                Utilities.JR_ADMIN_ROLE,
                Utilities.SR_MOD_ROLE,
                Utilities.JR_MOD_ROLE,
                Utilities.SUPPORT_ROLE,
                Utilities.SURVIVAL_ROLE,
                Utilities.LIFESTEAL_ROLE
        );

        for (Role r : roles) {
            if (r != null) trackedRoleIds.add(r.getIdLong());
        }
    }

    private boolean isRelevant(Collection<Role> roles) {
        for (Role r : roles) {
            if (trackedRoleIds.contains(r.getIdLong())) return true;
        }
        return false;
    }

    /**
     * Rebuild the entire V2 container and edit the latest bot message.
     * Deletes/recreates is optional.
     */
    private void refreshStaffMessage() {
        if (guild == null) return;

        TextChannel channel = Utilities.STAFF_LIST_CHANNEL;
        if (channel == null) return;

        // Fetch latest message sent by bot
        channel.getHistory().retrievePast(50).queue(messages -> {
            Optional<Message> botMessage = messages.stream()
                    .filter(m -> m.getAuthor().isBot())
                    .findFirst();

            botMessage.ifPresentOrElse(msg -> {
                // Edit the existing message with a completely rebuilt V2 container
                msg.editMessageComponents(
                        new StaffMessageBuilder().build(guild).withAccentColor(Color.BLUE)
                ).useComponentsV2().queue(
                        success -> System.out.println("Staff message refreshed successfully."),
                        error -> System.err.println("Failed to edit staff message: " + error.getMessage())
                );
            }, () -> {
                // No existing bot message found → send a new one
                channel.sendMessageComponents(
                        new StaffMessageBuilder().build(guild).withAccentColor(Color.BLUE)
                ).queue(
                        success -> System.out.println("Staff message sent for the first time."),
                        error -> System.err.println("Failed to send staff message: " + error.getMessage())
                );
            });
        });
    }
}
