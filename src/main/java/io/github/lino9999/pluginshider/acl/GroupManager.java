package io.github.lino9999.pluginshider.acl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class GroupManager {
    private final Map<String, AccessGroup> groups = new HashMap<>();
    private final Map<UUID, AccessGroup> playerGroupCache = new ConcurrentHashMap<>();
    private final ListRegistry listRegistry;
    private final Logger logger;
    private AccessGroup defaultGroup;

    public GroupManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.listRegistry = new ListRegistry(dataFolder, logger);
    }

    public void load(ConfigurationSection groupsSection) {
        groups.clear();
        playerGroupCache.clear();
        listRegistry.reload();

        if (groupsSection == null) {
            defaultGroup = new AccessGroup("default", null);
            defaultGroup.compileRules(listRegistry);
            groups.put("default", defaultGroup);
            return;
        }

        for (String groupKey : groupsSection.getKeys(false)) {
            ConfigurationSection sec = groupsSection.getConfigurationSection(groupKey);
            if (sec != null) {
                String normalized = groupKey.toLowerCase(Locale.ROOT);
                AccessGroup group = new AccessGroup(normalized, sec);
                group.compileRules(listRegistry);
                groups.put(normalized, group);
            }
        }

        for (AccessGroup group : groups.values()) {
            resolveInheritance(group, new HashSet<>());
        }

        defaultGroup = groups.get("default");
        if (defaultGroup == null) {
            defaultGroup = new AccessGroup("default", null);
            defaultGroup.compileRules(listRegistry);
            groups.put("default", defaultGroup);
        }
    }

    private void resolveInheritance(AccessGroup group, Set<String> visited) {
        if (visited.contains(group.getName())) {
            return;
        }
        visited.add(group.getName());
        for (String parentName : group.getInheritedGroupNames()) {
            AccessGroup parent = groups.get(parentName.toLowerCase(Locale.ROOT));
            if (parent != null) {
                resolveInheritance(parent, visited);
                group.mergeInheritedRules(parent);
            } else {
                logger.warning("Group '" + group.getName() + "' specifies unknown parent group: '" + parentName + "'");
            }
        }
    }

    public AccessGroup resolvePlayerGroup(Player player) {
        if (player == null) {
            return defaultGroup;
        }

        AccessGroup cached = playerGroupCache.get(player.getUniqueId());
        if (cached != null) {
            return cached;
        }

        AccessGroup selected = null;
        if (player.isOp() && groups.containsKey("op")) {
            selected = groups.get("op");
        } else if (player.hasPermission("*") && groups.containsKey("*")) {
            selected = groups.get("*");
        } else {
            int highestPriority = -1;
            for (Map.Entry<String, AccessGroup> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                AccessGroup group = entry.getValue();
                String permission = "pluginshider.group." + groupName;
                if (player.hasPermission(permission)) {
                    if (group.getPriority() > highestPriority) {
                        highestPriority = group.getPriority();
                        selected = group;
                    }
                }
            }
        }

        if (selected == null) {
            selected = defaultGroup;
        }

        playerGroupCache.put(player.getUniqueId(), selected);
        return selected;
    }

    public void invalidate(UUID uuid) {
        if (uuid != null) {
            playerGroupCache.remove(uuid);
        }
    }

    public void invalidateAll() {
        playerGroupCache.clear();
    }

    public AccessGroup getGroup(String name) {
        if (name == null) {
            return null;
        }
        return groups.get(name.toLowerCase(Locale.ROOT));
    }

    public Map<String, AccessGroup> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    public AccessGroup getDefaultGroup() {
        return defaultGroup;
    }

    public ListRegistry getListRegistry() {
        return listRegistry;
    }
}
