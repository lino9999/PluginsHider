package io.github.lino9999.pluginshider.config;

import io.github.lino9999.pluginshider.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;

public final class SpamSettings {
    public enum Action {
        KICK,
        BLOCK
    }

    private final boolean enabled;
    private final int windowSeconds;
    private final int maxCommands;
    private final Action action;
    private final String kickMessage;
    private final String blockedMessage;

    public SpamSettings(ConfigurationSection section) {
        if (section == null) {
            this.enabled = false;
            this.windowSeconds = 2;
            this.maxCommands = 30;
            this.action = Action.KICK;
            this.kickMessage = ColorUtil.colorize("&cExcessive command activity detected.");
            this.blockedMessage = ColorUtil.colorize("&cYou are sending commands too quickly. Please slow down.");
            return;
        }
        this.enabled = section.getBoolean("enabled", true);
        this.windowSeconds = Math.max(1, section.getInt("window-seconds", 2));
        this.maxCommands = Math.max(1, section.getInt("max-commands-per-window", 30));
        String actionStr = section.getString("action", "KICK");
        Action parsedAction;
        try {
            parsedAction = Action.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            parsedAction = Action.KICK;
        }
        this.action = parsedAction;
        this.kickMessage = ColorUtil.colorize(section.getString("kick-message", "&cExcessive command activity detected. Connection terminated."));
        this.blockedMessage = ColorUtil.colorize(section.getString("blocked-message", "&cYou are sending commands too quickly. Please slow down."));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public int getMaxCommands() {
        return maxCommands;
    }

    public Action getAction() {
        return action;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getBlockedMessage() {
        return blockedMessage;
    }
}
