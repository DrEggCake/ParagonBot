package com.dreggcake.src.TicketSystem;

import java.util.HashMap;
import java.util.Map;

public enum TicketType {
    TICKET_TYPE("ticket-type"),
    GENERAL("general"),
    APPEAL("appeal"),
    REPORT("report"),
    BUG("bug"),
    STORE("store");

    private static final Map<String, TicketType> BY_ID = new HashMap<>();

    static {
        for (TicketType b : values()) {
            BY_ID.put(b.id, b);
        }
    }

    private final String id;

    TicketType(String id) {
        this.id = id;
    }

    public static TicketType fromId(String id) {
        return BY_ID.get(id);
    }

    public String getId() {
        return id;
    }
}
