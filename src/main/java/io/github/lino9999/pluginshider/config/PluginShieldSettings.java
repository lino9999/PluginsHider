package io.github.lino9999.pluginshider.config;

import io.github.lino9999.pluginshider.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PluginShieldSettings {
    private final boolean enabled;
    private final Set<String> blockedCommands;
    private final String customResponse;

    public PluginShieldSettings(ConfigurationSection section) {
        if (section == null) {
            this.enabled = true;
            this.blockedCommands = defaultBlockedCommands();
            this.customResponse = ColorUtil.colorize("&8[&bPluginsHider&8] &cCommand access denied.");
            return;
        }
        this.enabled = section.getBoolean("enabled", true);
        List<String> list = section.getStringList("blocked-commands");
        Set<String> set = new HashSet<>();
        if (list.isEmpty()) {
            set.addAll(defaultBlockedCommands());
        } else {
            for (String cmd : list) {
                if (cmd != null && !cmd.isBlank()) {
                    String clean = cmd.trim().toLowerCase(Locale.ROOT);
                    while (clean.startsWith("/")) {
                        clean = clean.substring(1).trim();
                    }
                    set.add("/" + clean);
                }
            }
        }
        this.blockedCommands = Collections.unmodifiableSet(set);
        this.customResponse = ColorUtil.colorize(section.getString("custom-response", "&8[&bPluginsHider&8] &cCommand access denied."));
    }

    private static Set<String> defaultBlockedCommands() {
        Set<String> set = new HashSet<>();
        set.add("/plugins");
        set.add("/pl");
        set.add("/bukkit:plugins");
        set.add("/bukkit:pl");
        set.add("/version");
        set.add("/ver");
        set.add("/bukkit:version");
        set.add("/bukkit:ver");
        set.add("/about");
        set.add("/bukkit:about");
        set.add("/help");
        set.add("/bukkit:help");
        set.add("/?");
        set.add("/bukkit:?");
        return set;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isBlocked(String command) {
        if (!enabled || command == null) {
            return false;
        }
        String clean = command.toLowerCase(Locale.ROOT).trim();
        while (clean.startsWith("/")) {
            clean = clean.substring(1).trim();
        }
        clean = "/" + clean;
        int spaceIdx = clean.indexOf(' ');
        if (spaceIdx != -1) {
            clean = clean.substring(0, spaceIdx);
        }
        return blockedCommands.contains(clean);
    }

    public String getCustomResponse() {
        return customResponse;
    }

    public Set<String> getBlockedCommands() {
        return blockedCommands;
    }
}
