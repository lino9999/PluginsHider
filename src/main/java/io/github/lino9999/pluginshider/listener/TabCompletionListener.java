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
import java.util.Locale;

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
        boolean isAdmin = player.hasPermission("pluginshider.admin");

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

            String base = candidate;
            int space = base.indexOf(' ');
            if (space != -1) {
                base = base.substring(0, space);
            }

            if (isAdmin && isPluginAdminCommand(base)) {
                return false;
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

    private static boolean isPluginAdminCommand(String base) {
        if (base == null) {
            return false;
        }
        String clean = base.toLowerCase(Locale.ROOT);
        if (clean.startsWith("pluginshider:")) {
            clean = clean.substring(13);
        }
        return clean.equals("pluginshider") || clean.equals("phider") || clean.equals("ph") || clean.equals("plh");
    }
}
