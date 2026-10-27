package io.github.lino9999.pluginshider.security;

import io.github.lino9999.pluginshider.config.OperatorSettings;
import io.github.lino9999.pluginshider.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public final class OperatorGuard {
    private final Plugin plugin;
    private final Logger logger;

    public OperatorGuard(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void start(OperatorSettings settings) {
        if (!settings.isEnabled()) {
            return;
        }
        long interval = settings.getSweepIntervalSeconds();
        SchedulerUtil.runAsyncRepeating(plugin, () -> sweep(settings), interval, interval);
    }

    public void sweep(OperatorSettings settings) {
        if (!settings.isEnabled()) {
            return;
        }

        for (OfflinePlayer operator : Bukkit.getOperators()) {
            if (!settings.isAllowed(operator.getName(), operator.getUniqueId())) {
                SchedulerUtil.runGlobalSync(plugin, () -> {
                    if (operator.isOp() && !settings.isAllowed(operator.getName(), operator.getUniqueId())) {
                        if (settings.isAutoDeopUnauthorized()) {
                            operator.setOp(false);
                            logger.warning("Security enforcement: Revoked unauthorized operator status from " + operator.getName());
                        }
                    }
                });

                if (settings.isKickUnauthorized() && operator.isOnline()) {
                    Player online = operator.getPlayer();
                    if (online != null && online.isOnline()) {
                        SchedulerUtil.runOnPlayer(plugin, online, p -> p.kickPlayer(settings.getUnauthorizedKickMessage()));
                    }
                }
            }
        }
    }
}
