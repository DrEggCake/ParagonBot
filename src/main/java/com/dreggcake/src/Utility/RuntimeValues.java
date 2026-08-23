package com.dreggcake.src.Utility;

/**
 * Config values that can change at runtime via config reload.
 */
public final class RuntimeValues {

    public static String IP;
    public static String STORE;

    private RuntimeValues() {
    }

    public static void reload() {
        System.out.println("========== Loading Runtime Config Values ==========");
        IP = ConfigResolver.requireString("messages.ip");
        STORE = ConfigResolver.requireString("messages.store");
        System.out.println("========== Loading Runtime Config Values Finished ==========");
    }
}
