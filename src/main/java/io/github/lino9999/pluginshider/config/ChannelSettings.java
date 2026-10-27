package io.github.lino9999.pluginshider.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ChannelSettings {
    private final boolean enabled;
    private final Set<String> allowedChannels;

    public ChannelSettings(ConfigurationSection section) {
        if (section == null) {
            this.enabled = false;
            this.allowedChannels = Collections.emptySet();
            return;
        }
        this.enabled = section.getBoolean("enabled", true);
        List<String> list = section.getStringList("allowed-channels");
        Set<String> set = new HashSet<>();
        for (String item : list) {
            set.add(item.toLowerCase(Locale.ROOT).trim());
        }
        this.allowedChannels = Collections.unmodifiableSet(set);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isChannelAllowed(String channel) {
        if (!enabled) {
            return true;
        }
        if (channel == null) {
            return false;
        }
        return allowedChannels.contains(channel.toLowerCase(Locale.ROOT).trim());
    }

    public Set<String> getAllowedChannels() {
        return allowedChannels;
    }
}
