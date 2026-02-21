/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.dreggcake.src;

import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface ICommand {
    public String getName();

    public String getDescription();

    public List<OptionData> getOptions();

    public void execute(SlashCommandInteractionEvent event);
}

