package io.github.lino9999.pluginshider.util;

import org.bukkit.ChatColor;

public final class ColorUtil {
    private ColorUtil() {}

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
