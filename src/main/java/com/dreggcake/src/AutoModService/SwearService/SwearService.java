package com.dreggcake.src.AutoModService.SwearService;

import org.json.JSONArray;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class SwearService {

    private static final Path SWEAR_FILE = Path.of("swear/en.json");

    private static final List<Pattern> SWEAR_PATTERNS = new ArrayList<>();

    private static final int MIN_WORD_LENGTH = 3;

    // Fixed: removed '+' from obfuscations to avoid dangling meta character
    private static final Map<Character, String> OBFUSCATIONS = Map.of(
            'a', "[a@4]",
            'b', "[b8]",
            'e', "[e3]",
            'i', "[i1!|]",
            'l', "[l1|!]",
            'o', "[o0]",
            's', "[s$5]",
            't', "[t7]"
    );

    static {
        load();
    }

    private SwearService() {
    }

    private static void load() {
        System.out.println("[SwearService] Loading swear words...");

        try {
            if (!Files.exists(SWEAR_FILE)) {
                System.out.println("[SwearService] ❌ Missing swear file: " + SWEAR_FILE.toAbsolutePath());
                return;
            }

            String json = Files.readString(SWEAR_FILE);
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                String raw = array.getString(i).toLowerCase().trim();
                if (raw.length() < MIN_WORD_LENGTH) continue;

                String regex = buildFlexibleRegex(raw);
                SWEAR_PATTERNS.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            }

            System.out.println("[SwearService] ✅ Loaded patterns: " + SWEAR_PATTERNS.size());

        } catch (Exception e) {
            System.out.println("[SwearService] ❌ Failed to load swears");
            e.printStackTrace();
        }
    }

    /**
     * Public API
     */
    public static boolean containsSwear(String message) {
        if (message == null || message.isBlank()) return false;

        String normalized = normalize(message);

        for (Pattern pattern : SWEAR_PATTERNS) {
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helpers
     */

    private static String normalize(String input) {
        // Keep letters, numbers, and common obfuscation symbols
        return input.toLowerCase().replaceAll("[^a-z0-9@$!|]", " ");
    }

    private static String buildFlexibleRegex(String word) {
        StringBuilder sb = new StringBuilder("\\b"); // start word boundary

        for (char c : word.toCharArray()) {
            String pattern = OBFUSCATIONS.getOrDefault(c, Character.toString(c));

            // Escape any standalone regex metacharacters in pattern
            pattern = escapeRegexChars(pattern);

            // Wrap in non-capturing group if multiple options
            if (pattern.startsWith("[") && pattern.endsWith("]")) {
                sb.append(pattern);
            } else {
                sb.append("(?:").append(pattern).append(")");
            }

            // Repeat 1 or more times
            sb.append("+");

            // Optional whitespace
            sb.append("\\s*");
        }

        sb.append("\\b"); // end word boundary
        return sb.toString();
    }

    // Escape regex meta characters in a string (outside of character classes)
    private static String escapeRegexChars(String str) {
        return str.replaceAll("([\\\\.^$|?*+(){}\\[\\]])", "\\\\$1");
    }
}
