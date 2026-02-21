package com.dreggcake.src.Config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Config {

    private static Map<String, Object> data = new HashMap<>();
    private static final String CONFIG_FILE = "config.yml";

    // Load YAML from file, create if missing
    public static void load() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            System.out.println("[CONFIG] config.yml not found. Creating default.");
            saveDefaultConfig(file);
        }

        try (InputStream in = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(in);
            if (loaded instanceof Map) {
                data = (Map<String, Object>) loaded;
            } else {
                data = new HashMap<>();
            }
            System.out.println("[CONFIG] Loaded config.yml");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load config.yml");
        }
    }

    // Reload at runtime
    public static void reload() {
        load();
        System.out.println("[CONFIG] Reloaded config.yml");
    }

    // Save current data to disk
    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            yaml.dump(data, writer);
            System.out.println("[CONFIG] Saved config.yml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create default config
    private static void saveDefaultConfig(File file) {
        Map<String, Object> defaultData = new LinkedHashMap<>();
        defaultData.put("exampleString", "hello");
        defaultData.put("exampleInt", 123);
        defaultData.put("exampleBool", true);
        defaultData.put("exampleList", Arrays.asList("a", "b", "c"));
        defaultData.put("exampleMap", Map.of("key1", "value1", "key2", 42));

        data = defaultData;
        try (Writer writer = new FileWriter(file)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            yaml.dump(defaultData, writer);
            System.out.println("[CONFIG] Default config.yml created.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== GENERIC GETTER ======================
    private static Object get(String key) {
        if (key == null || key.isEmpty()) return null;
        String[] parts = key.split("\\.");
        Map<String, Object> current = data;
        Object value = null;

        for (int i = 0; i < parts.length; i++) {
            if (current == null) return null;
            value = current.get(parts[i]);
            if (i < parts.length - 1) {
                if (value instanceof Map) {
                    current = (Map<String, Object>) value;
                } else {
                    return null;
                }
            }
        }
        return value;
    }

    // ====================== TYPED GETTERS ======================
    public static String getString(String key, String defaultVal) {
        Object val = get(key);
        return val != null ? val.toString() : defaultVal;
    }

    public static int getInt(String key, int defaultVal) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        try { return val != null ? Integer.parseInt(val.toString()) : defaultVal; }
        catch (Exception e) { return defaultVal; }
    }

    public static double getDouble(String key, double defaultVal) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return val != null ? Double.parseDouble(val.toString()) : defaultVal; }
        catch (Exception e) { return defaultVal; }
    }

    public static boolean getBoolean(String key, boolean defaultVal) {
        Object val = get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return defaultVal;
    }

    public static List<String> getStringList(String key) {
        Object val = get(key);
        if (val instanceof List) return (List<String>) val;
        return Collections.emptyList();
    }

    public static List<Integer> getIntList(String key) {
        Object val = get(key);
        if (val instanceof List) return (List<Integer>) val;
        return Collections.emptyList();
    }

    public static Map<String,Object> getMap(String key) {
        Object val = get(key);
        if (val instanceof Map) return (Map<String,Object>) val;
        return Collections.emptyMap();
    }

    public static Object getRaw(String key) {
        return get(key);
    }

    // ====================== DEBUG UTILITIES ======================
    public static void printAll() {
        System.out.println("=== CONFIG VALUES ===");
        printMap("", data);
        System.out.println("=====================");
    }

    private static void printMap(String prefix, Map<String,Object> map) {
        if (map == null) return;
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object val = entry.getValue();
            if (val instanceof Map) {
                printMap(key, (Map<String,Object>) val);
            } else {
                System.out.println(key + " = " + val);
            }
        }
    }

    public static void checkKeys(List<String> requiredKeys) {
        List<String> missing = new ArrayList<>();
        for (String key : requiredKeys) if (get(key) == null) missing.add(key);
        if (!missing.isEmpty()) {
            System.out.println("[CONFIG] Missing required keys:");
            missing.forEach(k -> System.out.println(" - " + k));
        } else System.out.println("[CONFIG] All required keys are present.");
    }

    // ====================== MODIFY AT RUNTIME ======================
    public static void set(String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String,Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String,Object>();
                current.put(parts[i], next);
            }
            current = (Map<String,Object>) next;
        }
        current.put(parts[parts.length - 1], value);
    }
}
