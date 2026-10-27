package io.github.lino9999.pluginshider.listener;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Collection;
import java.util.Locale;

public final class TabSendListener implements Listener {
    private final PluginsHiderPlugin plugin;

    public TabSendListener(PluginsHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("pluginshider.bypass.tab")) {
            return;
        }

        AccessGroup group = plugin.getSettings().getGroupManager().resolvePlayerGroup(player);
        Collection<String> commands = event.getCommands();

        commands.removeIf(cmd -> {
            String normalized = cmd.toLowerCase(Locale.ROOT);
            if (plugin.getSettings().getPluginShieldSettings().isEnabled() && !player.hasPermission("pluginshider.bypass.plugins")) {
                if (plugin.getSettings().getPluginShieldSettings().isBlocked("/" + normalized)) {
                    return true;
                }
            }
            return !group.allowsRootTab(normalized);
        });
    }
}
