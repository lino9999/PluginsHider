package io.github.lino9999.pluginshider.config;

import io.github.lino9999.pluginshider.acl.GroupManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

public final class PluginSettings {
    private final boolean debug;
    private final boolean caseInsensitiveCommands;
    private final boolean hideColonCommands;
    private final String locale;
    private final BrandSettings brandSettings;
    private final ChannelSettings channelSettings;
    private final PluginShieldSettings pluginShieldSettings;
    private final SpamSettings spamSettings;
    private final OperatorSettings operatorSettings;
    private final GroupManager groupManager;

    public PluginSettings(FileConfiguration config, File dataFolder, Logger logger) {
        this.debug = config.getBoolean("settings.debug", false);
        this.caseInsensitiveCommands = config.getBoolean("settings.case-insensitive-commands", true);
        this.hideColonCommands = config.getBoolean("settings.hide-colon-commands", true);
        this.locale = config.getString("settings.locale", "en");

        this.brandSettings = new BrandSettings(config.getConfigurationSection("brand-spoofing"));
        this.channelSettings = new ChannelSettings(config.getConfigurationSection("channel-protection"));
        this.pluginShieldSettings = new PluginShieldSettings(config.getConfigurationSection("plugin-command-shield"));
        this.spamSettings = new SpamSettings(config.getConfigurationSection("rate-limiting"));
        this.operatorSettings = new OperatorSettings(config.getConfigurationSection("operator-security"));

        this.groupManager = new GroupManager(dataFolder, logger);
        this.groupManager.load(config.getConfigurationSection("groups"));
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isCaseInsensitiveCommands() {
        return caseInsensitiveCommands;
    }

    public boolean isHideColonCommands() {
        return hideColonCommands;
    }

    public String getLocale() {
        return locale;
    }

    public BrandSettings getBrandSettings() {
        return brandSettings;
    }

    public ChannelSettings getChannelSettings() {
        return channelSettings;
    }

    public PluginShieldSettings getPluginShieldSettings() {
        return pluginShieldSettings;
    }

    public SpamSettings getSpamSettings() {
        return spamSettings;
    }

    public OperatorSettings getOperatorSettings() {
        return operatorSettings;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}
