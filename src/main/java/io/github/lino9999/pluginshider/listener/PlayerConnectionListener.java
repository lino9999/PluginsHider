package io.github.lino9999.pluginshider.listener;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {
    private final PluginsHiderPlugin plugin;

    public PlayerConnectionListener(PluginsHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getSettings().getGroupManager().invalidate(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRateLimiter().remove(player.getUniqueId());
        plugin.getSettings().getGroupManager().invalidate(player.getUniqueId());
        plugin.getPacketEventsManager().cleanPlayer(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        plugin.getSettings().getGroupManager().invalidate(player.getUniqueId());
        SchedulerUtil.runOnPlayer(plugin, player, Player::updateCommands);
    }
}
