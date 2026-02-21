package com.dreggcake.src.Counting;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CountingCommands {

    private final CountingService service = new CountingService();
    private  CountingDAO dao = new CountingDAO();

    public void handle(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (!args[0].equalsIgnoreCase("!count")) return;

        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.MANAGE_CHANNEL)) return;

        long channelId = event.getChannel().getIdLong();

        switch (args.length >= 2 ? args[1].toLowerCase() : "") {

            case "enable" -> {
                CountingManager.enable(channelId);
                event.getChannel().sendMessage("✅ Counting enabled here.").queue();
                dao.save(CountingManager.get(channelId));
            }

            case "disable" -> {
                CountingManager.disable(channelId);
                event.getChannel().sendMessage("❌ Counting disabled here.").queue();
                dao.save(CountingManager.get(channelId));
            }

            case "set" -> {
                if (args.length < 3) return;

                long value;
                try {
                    value = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    return;
                }

                CountingState state = CountingManager.get(channelId);
                if (state == null) return;

                service.setCounter((net.dv8tion.jda.api.entities.channel.concrete.TextChannel) event.getChannel(), state, value);
            }
        }
    }
}
