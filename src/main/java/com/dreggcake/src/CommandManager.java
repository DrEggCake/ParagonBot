/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager
        extends ListenerAdapter {
    private List<ICommand> commands = new ArrayList<>();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            for (ICommand command : this.commands) {
                if (command.getOptions() == null) {
                    guild.upsertCommand(command.getName(), command.getDescription()).queue();
                    continue;
                }
                guild.upsertCommand(command.getName(), command.getDescription()).addOptions(command.getOptions()).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        for (ICommand command : this.commands) {
            if (!command.getName().equals(event.getName())) continue;
            command.execute(event);
            return;
        }
    }

    public void add(ICommand command) {
        this.commands.add(command);
    }
}

