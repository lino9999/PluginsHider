package io.github.lino9999.pluginshider.listener;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import io.github.lino9999.pluginshider.config.PluginSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.List;

public final class TabCompletionListener implements Listener {
    private final PluginsHiderPlugin plugin;

    public TabCompletionListener(PluginsHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) {
            return;
        }

        if (player.hasPermission("pluginshider.bypass.tab")) {
            return;
        }

        PluginSettings settings = plugin.getSettings();
        AccessGroup group = settings.getGroupManager().resolvePlayerGroup(player);
        String buffer = event.getBuffer();
        if (buffer == null) {
            return;
        }

        String clean = buffer.trim();
        while (clean.startsWith("/")) {
            clean = clean.substring(1).trim();
        }

        boolean bypassPlugins = player.hasPermission("pluginshider.bypass.plugins");
        boolean hideColon = settings.isHideColonCommands();

        String prefix = "";
        if (buffer.endsWith(" ")) {
            prefix = clean;
        } else {
            int lastSpace = clean.lastIndexOf(' ');
            if (lastSpace != -1) {
                prefix = clean.substring(0, lastSpace).trim();
            }
        }

        final String finalPrefix = prefix;
        List<String> completions = event.getCompletions();

        completions.removeIf(completion -> {
            String candidate = finalPrefix.isEmpty() ? completion : finalPrefix + " " + completion;
            while (candidate.startsWith("/")) {
                candidate = candidate.substring(1);
            }

            if (settings.getPluginShieldSettings().isEnabled() && !bypassPlugins) {
                if (settings.getPluginShieldSettings().isBlocked("/" + candidate)) {
                    return true;
                }
            }

            if (finalPrefix.isEmpty()) {
                return !group.allowsRootTab(candidate, hideColon);
            } else {
                return !group.allowsSubcommandTab(candidate, hideColon);
            }
        });
    }
}
