package io.github.lino9999.pluginshider.integration;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.util.SchedulerUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.sync.PostSyncEvent;
import net.luckperms.api.model.PermissionHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

public final class LuckPermsIntegration {
    private final PluginsHiderPlugin plugin;
    private final Logger logger;
    private boolean hooked;

    public LuckPermsIntegration(PluginsHiderPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void register() {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            return;
        }

        try {
            LuckPerms lp = LuckPermsProvider.get();
            EventBus eventBus = lp.getEventBus();

            eventBus.subscribe(plugin, NodeAddEvent.class, event -> {
                if (event.isUser()) {
                    handleHolder(event.getTarget());
                }
            });

            eventBus.subscribe(plugin, NodeRemoveEvent.class, event -> {
                if (event.isUser()) {
                    handleHolder(event.getTarget());
                }
            });

            eventBus.subscribe(plugin, PostSyncEvent.class, event -> {
                plugin.getSettings().getGroupManager().invalidateAll();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    refreshPlayer(player);
                }
            });

            hooked = true;
            logger.info("LuckPerms reactive permission hook active.");
        } catch (Throwable t) {
            logger.warning("Could not initialize LuckPerms integration: " + t.getMessage());
            hooked = false;
        }
    }

    private void handleHolder(PermissionHolder holder) {
        try {
            UUID uuid = UUID.fromString(holder.getIdentifier().getName());
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                refreshPlayer(player);
            }
        } catch (IllegalArgumentException ignored) {}
    }

    private void refreshPlayer(Player player) {
        plugin.getSettings().getGroupManager().invalidate(player.getUniqueId());
        SchedulerUtil.runOnPlayer(plugin, player, Player::updateCommands);
    }

    public boolean isHooked() {
        return hooked;
    }
}
