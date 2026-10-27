package io.github.lino9999.pluginshider.command;

import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import io.github.lino9999.pluginshider.util.ColorUtil;
import io.github.lino9999.pluginshider.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class PluginsHiderCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "check", "sync", "brand", "help");
    private final PluginsHiderPlugin plugin;

    public PluginsHiderCommand(PluginsHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pluginshider.admin")) {
            sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &cYou do not have permission to execute this command."));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload":
            case "rl":
                plugin.reloadPlugin();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SchedulerUtil.runOnPlayer(plugin, player, Player::updateCommands);
                }
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &aConfiguration, ACL groups, and custom lists successfully reloaded."));
                break;

            case "check":
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &cUsage: /" + label + " check <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &cPlayer '" + args[1] + "' is not online."));
                    return true;
                }
                AccessGroup group = plugin.getSettings().getGroupManager().resolvePlayerGroup(target);
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &7Player: &f" + target.getName()));
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &7Resolved Group: &b" + group.getName() + " &7(Priority: &f" + group.getPriority() + "&7, Mode: &f" + group.getMode() + "&7)"));
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &7Executable Rules: &a" + group.getCommandRules().size() + " &7| Tab Rules: &a" + group.getTabRules().size()));
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &7Inherits: &f" + String.join(", ", group.getInheritedGroupNames())));
                break;

            case "sync":
                if (args.length > 1) {
                    Player syncTarget = Bukkit.getPlayer(args[1]);
                    if (syncTarget == null) {
                        sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &cPlayer '" + args[1] + "' is not online."));
                        return true;
                    }
                    plugin.getSettings().getGroupManager().invalidate(syncTarget.getUniqueId());
                    SchedulerUtil.runOnPlayer(plugin, syncTarget, Player::updateCommands);
                    sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &aCommands synchronized for " + syncTarget.getName() + "."));
                } else {
                    plugin.getSettings().getGroupManager().invalidateAll();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        SchedulerUtil.runOnPlayer(plugin, online, Player::updateCommands);
                    }
                    sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &aCommands synchronized for all online players."));
                }
                break;

            case "brand":
                String brandText = plugin.getSettings().getBrandSettings().getRawBrand();
                boolean brandEnabled = plugin.getSettings().getBrandSettings().isEnabled();
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &7Brand Spoofing: " + (brandEnabled ? "&aEnabled" : "&cDisabled")));
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &7Configured Brand: &f" + brandText));
                break;

            default:
                sender.sendMessage(ColorUtil.colorize("&8[&bPluginsHider&8] &cUnknown subcommand. Type &f/" + label + " help &cfor available options."));
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtil.colorize("&b&lPluginsHider &7v" + plugin.getDescription().getVersion() + " &8| &7by &fLino"));
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtil.colorize("&b/pluginshider reload &8- &7Reload config and access lists"));
        sender.sendMessage(ColorUtil.colorize("&b/pluginshider check <player> &8- &7Inspect assigned group for a player"));
        sender.sendMessage(ColorUtil.colorize("&b/pluginshider sync [player] &8- &7Force-refresh client command tree"));
        sender.sendMessage(ColorUtil.colorize("&b/pluginshider brand &8- &7Show active server brand spoofing status"));
        sender.sendMessage(ColorUtil.colorize("&b/pluginshider help &8- &7Display this help message"));
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("pluginshider.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(prefix)) {
                    result.add(sub);
                }
            }
            return result;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("sync") || args[0].equalsIgnoreCase("info"))) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    names.add(player.getName());
                }
            }
            return names;
        }

        return Collections.emptyList();
    }
}
