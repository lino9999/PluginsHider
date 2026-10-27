package io.github.lino9999.pluginshider.acl;

import io.github.lino9999.pluginshider.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AccessGroup {
    public enum Mode {
        WHITELIST,
        BLACKLIST
    }

    private final String name;
    private final int priority;
    private final Mode mode;
    private final String blockedMessage;
    private final List<String> rawCommands;
    private final List<String> rawTabComplete;
    private final List<String> inheritedGroupNames;
    private final List<CommandRule> commandRules = new ArrayList<>();
    private final List<CommandRule> tabRules = new ArrayList<>();

    public AccessGroup(String name, ConfigurationSection section) {
        this.name = name.toLowerCase(Locale.ROOT);
        if (section == null) {
            this.priority = 0;
            this.mode = Mode.WHITELIST;
            this.blockedMessage = ColorUtil.colorize("&cUnknown command. Type \"/help\" for help.");
            this.rawCommands = Collections.emptyList();
            this.rawTabComplete = Collections.emptyList();
            this.inheritedGroupNames = Collections.emptyList();
            return;
        }
        this.priority = section.getInt("priority", 0);
        String modeStr = section.getString("mode", "WHITELIST");
        Mode parsedMode;
        try {
            parsedMode = Mode.valueOf(modeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            parsedMode = Mode.WHITELIST;
        }
        this.mode = parsedMode;
        this.blockedMessage = ColorUtil.colorize(section.getString("blocked-message", "&cUnknown command. Type \"/help\" for help."));
        this.rawCommands = section.getStringList("commands");
        this.rawTabComplete = section.getStringList("tab-complete");
        this.inheritedGroupNames = section.getStringList("inherited-groups");
    }

    public void compileRules(ListRegistry listRegistry) {
        commandRules.clear();
        tabRules.clear();

        List<String> expandedCommands = expandEntries(rawCommands, listRegistry);
        for (String raw : expandedCommands) {
            commandRules.add(new CommandRule(raw));
        }

        List<String> expandedTab = expandEntries(rawTabComplete, listRegistry);
        for (String raw : expandedTab) {
            tabRules.add(new CommandRule(raw));
        }
    }

    public void mergeInheritedRules(AccessGroup parent) {
        if (parent == null) {
            return;
        }
        for (CommandRule rule : parent.commandRules) {
            if (!containsRule(commandRules, rule)) {
                commandRules.add(rule);
            }
        }
        for (CommandRule rule : parent.tabRules) {
            if (!containsRule(tabRules, rule)) {
                tabRules.add(rule);
            }
        }
    }

    private boolean containsRule(List<CommandRule> rules, CommandRule target) {
        for (CommandRule existing : rules) {
            if (existing.getOriginal().equalsIgnoreCase(target.getOriginal())) {
                return true;
            }
        }
        return false;
    }

    private List<String> expandEntries(List<String> source, ListRegistry listRegistry) {
        List<String> result = new ArrayList<>();
        for (String item : source) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String trimmed = item.trim();
            boolean negated = trimmed.startsWith("!");
            String core = negated ? trimmed.substring(1).trim() : trimmed;

            if (core.toLowerCase(Locale.ROOT).startsWith("list:")) {
                String targetList = core.substring(5).trim();
                List<String> fromList = listRegistry.getList(targetList);
                for (String fromEntry : fromList) {
                    result.add(negated ? "!" + fromEntry : fromEntry);
                }
            } else {
                result.add(trimmed);
            }
        }
        return result;
    }

    public AccessDecision evaluateCommand(String commandLine) {
        if (commandLine == null) {
            return AccessDecision.DENY;
        }

        for (CommandRule rule : commandRules) {
            if (rule.isNegated() && rule.matches(commandLine)) {
                return AccessDecision.DENY;
            }
        }

        if (mode == Mode.WHITELIST) {
            for (CommandRule rule : commandRules) {
                if (!rule.isNegated() && rule.matches(commandLine)) {
                    return AccessDecision.ALLOW;
                }
            }
            return AccessDecision.DENY;
        } else {
            for (CommandRule rule : commandRules) {
                if (!rule.isNegated() && rule.matches(commandLine)) {
                    return AccessDecision.DENY;
                }
            }
            return AccessDecision.ALLOW;
        }
    }

    public boolean allowsRootTab(String rootCommand) {
        if (rootCommand == null) {
            return false;
        }

        for (CommandRule rule : tabRules) {
            if (rule.isNegated() && rule.matchesRoot(rootCommand)) {
                return false;
            }
        }

        if (mode == Mode.WHITELIST) {
            for (CommandRule rule : tabRules) {
                if (!rule.isNegated() && rule.matchesRoot(rootCommand)) {
                    return true;
                }
            }
            return false;
        } else {
            for (CommandRule rule : tabRules) {
                if (!rule.isNegated() && rule.matchesRoot(rootCommand)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean allowsSubcommandTab(String fullCommandPrefix) {
        if (fullCommandPrefix == null) {
            return false;
        }

        for (CommandRule rule : tabRules) {
            if (rule.isNegated() && rule.matches(fullCommandPrefix)) {
                return false;
            }
        }

        if (mode == Mode.WHITELIST) {
            for (CommandRule rule : tabRules) {
                if (!rule.isNegated() && rule.matches(fullCommandPrefix)) {
                    return true;
                }
            }
            return false;
        } else {
            for (CommandRule rule : tabRules) {
                if (!rule.isNegated() && rule.matches(fullCommandPrefix)) {
                    return false;
                }
            }
            return true;
        }
    }

    public String formatBlockedMessage(String playerName, String command) {
        if (blockedMessage == null || blockedMessage.equalsIgnoreCase("none")) {
            return null;
        }
        return blockedMessage
                .replace("{player}", playerName != null ? playerName : "")
                .replace("{command}", command != null ? command : "");
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public Mode getMode() {
        return mode;
    }

    public String getBlockedMessage() {
        return blockedMessage;
    }

    public List<String> getInheritedGroupNames() {
        return inheritedGroupNames;
    }

    public List<CommandRule> getCommandRules() {
        return commandRules;
    }

    public List<CommandRule> getTabRules() {
        return tabRules;
    }
}
