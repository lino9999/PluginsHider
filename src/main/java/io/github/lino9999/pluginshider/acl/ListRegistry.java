package io.github.lino9999.pluginshider.acl;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class ListRegistry {
    private final Map<String, List<String>> definedLists = new HashMap<>();
    private final File dataFolder;
    private final Logger logger;

    public ListRegistry(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void reload() {
        definedLists.clear();
        File listsDir = new File(dataFolder, "lists");
        if (!listsDir.exists()) {
            listsDir.mkdirs();
        }

        File[] files = listsDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }

        Map<String, List<String>> rawMap = new HashMap<>();
        for (File file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            for (String key : yaml.getKeys(false)) {
                String normalized = key.toLowerCase(Locale.ROOT);
                List<String> entries = yaml.getStringList(key);
                if (!rawMap.containsKey(normalized)) {
                    rawMap.put(normalized, entries);
                } else {
                    logger.warning("Duplicate command list key found: " + key + " in " + file.getName());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : rawMap.entrySet()) {
            List<String> resolved = resolveRecursive(entry.getKey(), entry.getValue(), rawMap, new HashSet<>());
            definedLists.put(entry.getKey(), Collections.unmodifiableList(resolved));
        }
    }

    private List<String> resolveRecursive(String key, List<String> entries, Map<String, List<String>> rawMap, Set<String> visiting) {
        if (visiting.contains(key)) {
            logger.warning("Circular list reference detected for list: " + key);
            return Collections.emptyList();
        }
        visiting.add(key);

        List<String> result = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String trimmed = entry.trim();
            boolean negated = trimmed.startsWith("!");
            String core = negated ? trimmed.substring(1).trim() : trimmed;

            if (core.toLowerCase(Locale.ROOT).startsWith("list:")) {
                String target = core.substring(5).trim().toLowerCase(Locale.ROOT);
                if (rawMap.containsKey(target)) {
                    List<String> expanded = resolveRecursive(target, rawMap.get(target), rawMap, new HashSet<>(visiting));
                    for (String exp : expanded) {
                        result.add(negated ? "!" + exp : exp);
                    }
                } else {
                    logger.warning("Unresolved list reference '" + target + "' in list: " + key);
                }
            } else {
                result.add(trimmed);
            }
        }
        return result;
    }

    public List<String> getList(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        return definedLists.getOrDefault(name.toLowerCase(Locale.ROOT).trim(), Collections.emptyList());
    }

    public boolean hasList(String name) {
        if (name == null) {
            return false;
        }
        return definedLists.containsKey(name.toLowerCase(Locale.ROOT).trim());
    }
}
