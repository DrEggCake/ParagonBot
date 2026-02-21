/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src.Commands.Ticket;

import com.dreggcake.src.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class TicketClose
        implements ICommand {

    @Override
    public String getName() {
        return "ticket-close";
    }

    @Override
    public String getDescription() {
        return "Close a ticket";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
    }
}

