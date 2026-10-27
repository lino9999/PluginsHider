package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete.CommandMatch;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import io.github.lino9999.pluginshider.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TabCompleteProcessor {
    private TabCompleteProcessor() {}

    public static void filterTabComplete(PacketSendEvent event, Player player, PluginSettings settings, String inputText) {
        if (player == null && event.getUser() != null && event.getUser().getUUID() != null) {
            player = Bukkit.getPlayer(event.getUser().getUUID());
        }

        if (player == null || player.hasPermission("pluginshider.bypass.tab")) {
            return;
        }

        AccessGroup group = settings.getGroupManager().resolvePlayerGroup(player);
        WrapperPlayServerTabComplete wrapper = new WrapperPlayServerTabComplete(event);
        List<CommandMatch> matches = wrapper.getCommandMatches();
        if (matches == null || matches.isEmpty()) {
            return;
        }

        String prefix = "";
        if (inputText != null && !inputText.isBlank()) {
            String cleanInput = inputText.trim();
            while (cleanInput.startsWith("/")) {
                cleanInput = cleanInput.substring(1).trim();
            }
            if (inputText.endsWith(" ")) {
                prefix = cleanInput;
            } else {
                int lastSpace = cleanInput.lastIndexOf(' ');
                if (lastSpace != -1) {
                    prefix = cleanInput.substring(0, lastSpace).trim();
                }
            }
        }

        List<CommandMatch> filtered = new ArrayList<>();
        boolean bypassPlugins = player.hasPermission("pluginshider.bypass.plugins");
        boolean hideColon = settings.isHideColonCommands();
        boolean isAdmin = player.hasPermission("pluginshider.admin");

        for (CommandMatch match : matches) {
            String text = match.getText();
            if (text == null) {
                continue;
            }

            String clean = text.trim();
            while (clean.startsWith("/")) {
                clean = clean.substring(1);
            }

            String candidate = prefix.isEmpty() ? clean : prefix + " " + clean;

            String base = candidate;
            int space = base.indexOf(' ');
            if (space != -1) {
                base = base.substring(0, space);
            }

            if (isAdmin && isPluginAdminCommand(base)) {
                filtered.add(match);
                continue;
            }

            if (settings.getPluginShieldSettings().isEnabled() && !bypassPlugins) {
                if (settings.getPluginShieldSettings().isBlocked("/" + candidate)) {
                    continue;
                }
            }

            if (prefix.isEmpty()) {
                if (group.allowsRootTab(candidate, hideColon)) {
                    filtered.add(match);
                }
            } else {
                if (group.allowsSubcommandTab(candidate, hideColon)) {
                    filtered.add(match);
                }
            }
        }

        if (filtered.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        wrapper.setCommandMatches(filtered);
        event.markForReEncode(true);
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
