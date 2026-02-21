package com.dreggcake.src.AutoModService.SwearService;

import org.json.JSONArray;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class SwearWordProvider {

    private static final Set<String> SWEAR_WORDS = new HashSet<>();
    private static final Path SWEAR_FILE = Path.of("swear/en.json");

    static {
        load();
    }

    private static void load() {
        System.out.println("[SwearService] Loading swear words...");

        try {
            if (!Files.exists(SWEAR_FILE)) {
                System.out.println("[SwearService] ❌ File not found: " + SWEAR_FILE.toAbsolutePath());
                return;
            }

            String json = Files.readString(SWEAR_FILE);

            JSONArray words = new JSONArray(json); // ✅ ARRAY, not object

            for (int i = 0; i < words.length(); i++) {
                SWEAR_WORDS.add(
                        words.getString(i)
                                .toLowerCase()
                                .replaceAll("[^a-z0-9 ]", "")
                                .trim()
                );
            }

            System.out.println("[SwearService] ✅ Loaded " + SWEAR_WORDS.size() + " swear words");

        } catch (Exception e) {
            System.out.println("[SwearService] ❌ Failed to load swear words");
            e.printStackTrace();
        }
    }


    public static Set<String> getWords() {
        return SWEAR_WORDS;
    }
}
