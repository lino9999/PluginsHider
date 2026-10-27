package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.lino9999.pluginshider.PluginsHiderPlugin;

import java.util.logging.Logger;

public final class PacketEventsManager {
    private final PluginsHiderPlugin plugin;
    private final Logger logger;
    private PacketEventListener listener;
    private boolean active;

    public PacketEventsManager(PluginsHiderPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void register() {
        try {
            listener = new PacketEventListener(plugin);
            PacketEvents.getAPI().getEventManager().registerListener(listener);
            active = true;
            logger.info("PacketEvents integration successfully hooked.");
        } catch (Throwable t) {
            logger.warning("PacketEvents hook failed: " + t.getMessage());
            active = false;
        }
    }

    public void unregister() {
        if (active && listener != null) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(listener);
            } catch (Throwable ignored) {}
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }
}
