package io.github.lino9999.pluginshider.listener;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.config.OperatorSettings;
import io.github.lino9999.pluginshider.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
                SchedulerUtil.runGlobalSync(plugin, () -> {
                    if (op.isOp() && !settings.isAllowed(op.getName(), op.getUniqueId())) {
                        op.setOp(false);
                    }
                });
            }
            if (settings.isKickUnauthorized()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, settings.getUnauthorizedKickMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinSecurity(PlayerJoinEvent event) {
        OperatorSettings settings = plugin.getSettings().getOperatorSettings();
        if (!settings.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isOp() && !settings.isAllowed(player.getName(), player.getUniqueId())) {
            if (settings.isAutoDeopUnauthorized()) {
                SchedulerUtil.runGlobalSync(plugin, () -> {
                    if (player.isOp() && !settings.isAllowed(player.getName(), player.getUniqueId())) {
                        player.setOp(false);
                        plugin.getLogger().warning("Security enforcement: Revoked unauthorized operator status from " + player.getName());
                    }
                });
            }
            if (settings.isKickUnauthorized()) {
                SchedulerUtil.runOnPlayer(plugin, player, p -> p.kickPlayer(settings.getUnauthorizedKickMessage()));
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
        while (msg.startsWith("/")) {
            msg = msg.substring(1).trim();
        }
        if (msg.startsWith("op ") || msg.startsWith("minecraft:op ")) {
            Player sender = event.getPlayer();
            if (!settings.isAllowed(sender.getName(), sender.getUniqueId())) {
                event.setCancelled(true);
                sender.sendMessage(settings.getBlockOpCommandMessage());
                return;
            }

            String[] tokens = msg.split("\\s+");
            if (tokens.length > 1) {
                String targetPlayer = tokens[1];
                if (!settings.isAllowed(targetPlayer, null)) {
                    event.setCancelled(true);
                    sender.sendMessage(settings.getBlockOpCommandMessage());
                }
            }
        } else if (msg.startsWith("deop ") || msg.startsWith("minecraft:deop ")) {
            Player sender = event.getPlayer();
            String[] tokens = msg.split("\\s+");
            if (tokens.length > 1) {
                String targetPlayer = tokens[1];
                if (settings.isAllowed(targetPlayer, null) && !settings.isAllowed(sender.getName(), sender.getUniqueId())) {
                    event.setCancelled(true);
                    sender.sendMessage(settings.getBlockOpCommandMessage());
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
        while (cmd.startsWith("/")) {
            cmd = cmd.substring(1).trim();
        }
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
