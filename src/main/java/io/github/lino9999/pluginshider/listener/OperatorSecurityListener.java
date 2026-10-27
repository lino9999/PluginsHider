package io.github.lino9999.pluginshider.listener;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.config.OperatorSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Locale;
import java.util.UUID;

public final class OperatorSecurityListener implements Listener {
    private final PluginsHiderPlugin plugin;

    public OperatorSecurityListener(PluginsHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        OperatorSettings settings = plugin.getSettings().getOperatorSettings();
        if (!settings.isEnabled()) {
            return;
        }

        UUID uuid = event.getUniqueId();
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (op.isOp() && !settings.isAllowed(event.getName(), uuid)) {
            if (settings.isAutoDeopUnauthorized()) {
                op.setOp(false);
            }
            if (settings.isKickUnauthorized()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, settings.getUnauthorizedKickMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerOpCommand(PlayerCommandPreprocessEvent event) {
        OperatorSettings settings = plugin.getSettings().getOperatorSettings();
        if (!settings.isEnabled()) {
            return;
        }

        String msg = event.getMessage().trim().toLowerCase(Locale.ROOT);
        if (msg.startsWith("/op ") || msg.startsWith("/minecraft:op ")) {
            String[] tokens = msg.split("\\s+");
            if (tokens.length > 1) {
                String targetPlayer = tokens[1];
                if (!settings.isAllowed(targetPlayer, null)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(settings.getBlockOpCommandMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerOpCommand(ServerCommandEvent event) {
        OperatorSettings settings = plugin.getSettings().getOperatorSettings();
        if (!settings.isEnabled()) {
            return;
        }

        String cmd = event.getCommand().trim().toLowerCase(Locale.ROOT);
        if (cmd.startsWith("op ") || cmd.startsWith("minecraft:op ")) {
            String[] tokens = cmd.split("\\s+");
            if (tokens.length > 1) {
                String targetPlayer = tokens[1];
                if (!settings.isAllowed(targetPlayer, null)) {
                    event.setCancelled(true);
                    event.getSender().sendMessage(settings.getBlockOpCommandMessage());
                }
            }
        }
    }
}
