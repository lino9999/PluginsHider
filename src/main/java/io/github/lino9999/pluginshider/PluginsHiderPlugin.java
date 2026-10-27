package io.github.lino9999.pluginshider;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.lino9999.pluginshider.command.PluginsHiderCommand;
import io.github.lino9999.pluginshider.config.PluginSettings;
import io.github.lino9999.pluginshider.integration.LuckPermsIntegration;
import io.github.lino9999.pluginshider.listener.CommandExecutionListener;
import io.github.lino9999.pluginshider.listener.OperatorSecurityListener;
import io.github.lino9999.pluginshider.listener.PlayerConnectionListener;
import io.github.lino9999.pluginshider.listener.TabCompletionListener;
import io.github.lino9999.pluginshider.listener.TabSendListener;
import io.github.lino9999.pluginshider.packet.PacketEventsManager;
import io.github.lino9999.pluginshider.security.CommandRateLimiter;
import io.github.lino9999.pluginshider.security.OperatorGuard;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class PluginsHiderPlugin extends JavaPlugin {
    private PluginSettings settings;
    private CommandRateLimiter rateLimiter;
    private OperatorGuard operatorGuard;
    private PacketEventsManager packetEventsManager;
    private LuckPermsIntegration luckPermsIntegration;

    @Override
    public void onLoad() {
        if (PacketEvents.getAPI() == null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        }
        if (!PacketEvents.getAPI().isLoaded()) {
            PacketEvents.getAPI().load();
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultLists();

        this.settings = new PluginSettings(getConfig(), getDataFolder(), getLogger());
        this.rateLimiter = new CommandRateLimiter();
        this.operatorGuard = new OperatorGuard(this, getLogger());
        this.packetEventsManager = new PacketEventsManager(this, getLogger());
        this.luckPermsIntegration = new LuckPermsIntegration(this, getLogger());

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CommandExecutionListener(this), this);
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new TabSendListener(this), this);
        pm.registerEvents(new TabCompletionListener(this), this);
        pm.registerEvents(new OperatorSecurityListener(this), this);

        PluginsHiderCommand commandHandler = new PluginsHiderCommand(this);
        PluginCommand cmd = getCommand("pluginshider");
        if (cmd != null) {
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        }

        if (!PacketEvents.getAPI().isInitialized()) {
            PacketEvents.getAPI().init();
        }
        this.packetEventsManager.register();
        this.luckPermsIntegration.register();

        if (settings.getOperatorSettings().isEnabled()) {
            this.operatorGuard.start(settings.getOperatorSettings());
        }

        getLogger().info("PluginsHider v" + getDescription().getVersion() + " initialized successfully by Lino.");
    }

    @Override
    public void onDisable() {
        if (packetEventsManager != null) {
            packetEventsManager.unregister();
        }
        if (PacketEvents.getAPI() != null && PacketEvents.getAPI().isInitialized()) {
            PacketEvents.getAPI().terminate();
        }
        if (rateLimiter != null) {
            rateLimiter.clear();
        }
        getLogger().info("PluginsHider disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        this.settings = new PluginSettings(getConfig(), getDataFolder(), getLogger());
        if (this.rateLimiter != null) {
            this.rateLimiter.clear();
        }
        if (this.operatorGuard != null && settings.getOperatorSettings().isEnabled()) {
            this.operatorGuard.sweep(settings.getOperatorSettings());
        }
        getLogger().info("PluginsHider reloaded.");
    }

    private void saveDefaultLists() {
        File listDir = new File(getDataFolder(), "lists");
        if (!listDir.exists()) {
            listDir.mkdirs();
        }
        File sampleFile = new File(listDir, "utility-commands.yml");
        if (!sampleFile.exists()) {
            try (InputStream in = getResource("lists/utility-commands.yml")) {
                if (in != null) {
                    Files.copy(in, sampleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ignored) {}
        }
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public CommandRateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public OperatorGuard getOperatorGuard() {
        return operatorGuard;
    }

    public PacketEventsManager getPacketEventsManager() {
        return packetEventsManager;
    }

    public LuckPermsIntegration getLuckPermsIntegration() {
        return luckPermsIntegration;
    }
}
