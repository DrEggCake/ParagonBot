package com.dreggcake.src.TicketSystem;

import java.util.HashMap;
import java.util.Map;

public enum TicketButton {

    CREATE("create-ticket");

    private final String id;

    private static final Map<String, TicketButton> BY_ID = new HashMap<>();

    static {
        for (TicketButton b : values()){
            BY_ID.put(b.id, b);
        }
    }

    TicketButton(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public static TicketButton fromId(String id){
        return BY_ID.get(id);
    }
}
