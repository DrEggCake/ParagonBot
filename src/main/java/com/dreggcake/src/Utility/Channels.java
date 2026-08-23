package com.dreggcake.src.Utility;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * All configured text channels and categories, resolved once at startup.
 */
public final class Channels {

    // ================== Logs ==================
    public static TextChannel PUNISHMENT_LOG;
    public static TextChannel MEMBER_LOG;
    public static TextChannel MESSAGES_LOG;
    public static TextChannel INVITE_LOG;
    public static TextChannel TICKET_LOG;
    public static TextChannel MEDIA_LOG;

    // ================== Tickets ==================
    public static TextChannel TICKET;

    // ================== General ==================
    public static TextChannel WELCOME;
    public static TextChannel COUNTING;
    public static TextChannel SPAM;
    public static TextChannel STAFF_LIST;
    public static TextChannel SUPPORT;
    public static TextChannel DO_NOT_DELETE;

    // ================== Media / Partners ==================
    public static TextChannel MEDIA_UPLOAD;
    public static TextChannel PARTNER_QUEEN;

    // ================== Categories ==================
    public static Category TICKET_CATEGORY;
    public static Category STAFF_LOGS_CATEGORY;

    private Channels() {
    }

    public static void load(Guild guild) {
        PUNISHMENT_LOG = ConfigResolver.textChannel(guild, "ChannelsID.punishmentLogID");
        MEMBER_LOG = ConfigResolver.textChannel(guild, "ChannelsID.memberLogID");
        MESSAGES_LOG = ConfigResolver.textChannel(guild, "ChannelsID.messagesLogID");
        INVITE_LOG = ConfigResolver.textChannel(guild, "ChannelsID.inviteLogID");
        TICKET_LOG = ConfigResolver.textChannel(guild, "ChannelsID.ticketLogID");
        MEDIA_LOG = ConfigResolver.textChannel(guild, "ChannelsID.mediaLogID");

        TICKET = ConfigResolver.textChannel(guild, "ChannelsID.ticketID");

        WELCOME = ConfigResolver.textChannel(guild, "ChannelsID.welcomeID");
        COUNTING = ConfigResolver.textChannel(guild, "autoModBypassChannelsID.countingID");
        SPAM = ConfigResolver.textChannel(guild, "autoModBypassChannelsID.spammingID");
        STAFF_LIST = ConfigResolver.textChannel(guild, "ChannelsID.staffListChannelID");
        SUPPORT = ConfigResolver.textChannel(guild, "ChannelsID.supportChannelID");
        DO_NOT_DELETE = ConfigResolver.textChannel(guild, "ChannelsID.doNotDeleteID");

        MEDIA_UPLOAD = ConfigResolver.textChannel(guild, "media.channels.mediaUploadID");
        PARTNER_QUEEN = ConfigResolver.textChannel(guild, "Partner.queenChannel");

        TICKET_CATEGORY = ConfigResolver.category(guild, "CategoryID.ticketCategoryID");
        STAFF_LOGS_CATEGORY = ConfigResolver.category(guild, "CategoryID.staffLogsId");
    }
}
