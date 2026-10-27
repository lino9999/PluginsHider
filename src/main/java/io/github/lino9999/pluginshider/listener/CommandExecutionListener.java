package io.github.lino9999.pluginshider.listener;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.acl.AccessDecision;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import io.github.lino9999.pluginshider.config.PluginSettings;
import io.github.lino9999.pluginshider.config.SpamSettings;
import io.github.lino9999.pluginshider.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public final class CommandExecutionListener implements Listener {
    private final PluginsHiderPlugin plugin;

    public CommandExecutionListener(PluginsHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PluginSettings settings = plugin.getSettings();

        if (settings.getSpamSettings().isEnabled() && !player.hasPermission("pluginshider.bypass.spam")) {
            boolean exceeded = plugin.getRateLimiter().recordAndCheckExceeded(player.getUniqueId(), settings.getSpamSettings());
            if (exceeded) {
                event.setCancelled(true);
                if (settings.getSpamSettings().getAction() == SpamSettings.Action.KICK) {
                    SchedulerUtil.runOnPlayer(plugin, player, p -> p.kickPlayer(settings.getSpamSettings().getKickMessage()));
                } else {
                    String blockMsg = settings.getSpamSettings().getBlockedMessage();
                    if (blockMsg != null && !blockMsg.isEmpty() && !blockMsg.equalsIgnoreCase("none")) {
                        player.sendMessage(blockMsg);
                    }
                }
                return;
            }
        }

        String rawMessage = event.getMessage();
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }

        if (settings.isCaseInsensitiveCommands()) {
            String[] tokens = rawMessage.split("\\s+", 2);
            tokens[0] = tokens[0].toLowerCase(Locale.ROOT);
            if (tokens.length > 1) {
                rawMessage = tokens[0] + " " + tokens[1];
            } else {
                rawMessage = tokens[0];
            }
            event.setMessage(rawMessage);
        }

        String commandLine = rawMessage.trim();
        while (commandLine.startsWith("/")) {
            commandLine = commandLine.substring(1).trim();
        }

        if (commandLine.isEmpty()) {
            return;
        }

        String base = commandLine;
        int space = base.indexOf(' ');
        if (space != -1) {
            base = base.substring(0, space);
        }

        if (isPluginAdminCommand(base) && player.hasPermission("pluginshider.admin")) {
            return;
        }

        if (settings.getPluginShieldSettings().isEnabled() && !player.hasPermission("pluginshider.bypass.plugins")) {
            if (settings.getPluginShieldSettings().isBlocked(rawMessage)) {
                event.setCancelled(true);
                String response = settings.getPluginShieldSettings().getCustomResponse();
                if (response != null && !response.equalsIgnoreCase("none") && !response.isEmpty()) {
                    player.sendMessage(response);
                }
                return;
            }
        }

        if (player.hasPermission("pluginshider.bypass.commands")) {
            return;
        }

        AccessGroup group = settings.getGroupManager().resolvePlayerGroup(player);
        AccessDecision decision = group.evaluateCommand(commandLine, settings.isHideColonCommands());

        if (decision == AccessDecision.DENY) {
            event.setCancelled(true);
            String blockedMsg = group.formatBlockedMessage(player.getName(), commandLine);
            if (blockedMsg != null && !blockedMsg.isEmpty()) {
                player.sendMessage(blockedMsg);
            }
        }
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
