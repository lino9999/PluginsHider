package io.github.lino9999.pluginshider.config;

import io.github.lino9999.pluginshider.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class OperatorSettings {
    private final boolean enabled;
    private final Set<String> allowedNames;
    private final Set<UUID> allowedUuids;
    private final boolean autoDeopUnauthorized;
    private final boolean kickUnauthorized;
    private final String unauthorizedKickMessage;
    private final String blockOpCommandMessage;
    private final int sweepIntervalSeconds;

    public OperatorSettings(ConfigurationSection section) {
        if (section == null) {
            this.enabled = false;
            this.allowedNames = Collections.emptySet();
            this.allowedUuids = Collections.emptySet();
            this.autoDeopUnauthorized = true;
            this.kickUnauthorized = true;
            this.unauthorizedKickMessage = ColorUtil.colorize("&cUnauthorized operator privilege detected.");
            this.blockOpCommandMessage = ColorUtil.colorize("&cYou are not authorized to assign operator privileges.");
            this.sweepIntervalSeconds = 5;
            return;
        }
        this.enabled = section.getBoolean("enabled", false);
        this.autoDeopUnauthorized = section.getBoolean("auto-deop-unauthorized", true);
        this.kickUnauthorized = section.getBoolean("kick-unauthorized", true);
        this.unauthorizedKickMessage = ColorUtil.colorize(section.getString("unauthorized-kick-message", "&cUnauthorized operator privilege detected."));
        this.blockOpCommandMessage = ColorUtil.colorize(section.getString("block-op-command-message", "&cYou are not authorized to assign operator privileges."));
        this.sweepIntervalSeconds = Math.max(1, section.getInt("sweep-interval-seconds", 5));

        List<String> list = section.getStringList("allowed-operators");
        Set<String> names = new HashSet<>();
        Set<UUID> uuids = new HashSet<>();
        for (String entry : list) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            try {
                UUID parsedUuid = UUID.fromString(entry.trim());
                uuids.add(parsedUuid);
            } catch (IllegalArgumentException e) {
                names.add(entry.trim().toLowerCase(Locale.ROOT));
            }
        }
        this.allowedNames = Collections.unmodifiableSet(names);
        this.allowedUuids = Collections.unmodifiableSet(uuids);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAllowed(String name, UUID uuid) {
        if (!enabled) {
            return true;
        }
        if (uuid != null && allowedUuids.contains(uuid)) {
            return true;
        }
        if (name != null && allowedNames.contains(name.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return false;
    }

    public boolean isAutoDeopUnauthorized() {
        return autoDeopUnauthorized;
    }

    public boolean isKickUnauthorized() {
        return kickUnauthorized;
    }

    public String getUnauthorizedKickMessage() {
        return unauthorizedKickMessage;
    }

    public String getBlockOpCommandMessage() {
        return blockOpCommandMessage;
    }

    public int getSweepIntervalSeconds() {
        return sweepIntervalSeconds;
    }

    public Set<String> getAllowedNames() {
        return allowedNames;
    }

    public Set<UUID> getAllowedUuids() {
        return allowedUuids;
    }
}
