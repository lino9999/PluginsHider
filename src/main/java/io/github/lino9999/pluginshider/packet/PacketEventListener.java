package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketEventListener extends PacketListenerAbstract {
    private final PluginsHiderPlugin plugin;
    private final Map<String, String> tabRequestMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastPlayerTabMap = new ConcurrentHashMap<>();

    public PacketEventListener(PluginsHiderPlugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    public void cleanPlayer(UUID uuid) {
        if (uuid == null) {
            return;
        }
        tabRequestMap.keySet().removeIf(k -> k.startsWith(uuid.toString()));
        lastPlayerTabMap.remove(uuid);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
            UUID uuid = null;
            if (event.getPlayer() instanceof Player p) {
                uuid = p.getUniqueId();
            } else if (event.getUser() != null) {
                uuid = event.getUser().getUUID();
            }

            if (uuid != null) {
                String text = wrapper.getText();
                lastPlayerTabMap.put(uuid, text);
                Optional<Integer> idOpt = wrapper.getTransactionId();
                if (idOpt.isPresent()) {
                    if (tabRequestMap.size() > 5000) {
                        tabRequestMap.clear();
                    }
                    tabRequestMap.put(uuid + ":" + idOpt.get(), text);
                }
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PluginSettings settings = plugin.getSettings();
        if (settings == null) {
            return;
        }

        Player player = null;
        Object pObj = event.getPlayer();
        if (pObj instanceof Player p) {
            player = p;
        } else if (event.getUser() != null && event.getUser().getUUID() != null) {
            player = Bukkit.getPlayer(event.getUser().getUUID());
        }

        if (event.getPacketType() == PacketType.Play.Server.PLUGIN_MESSAGE) {
            WrapperPlayServerPluginMessage wrapper = new WrapperPlayServerPluginMessage(event);
            if (BrandPacketProcessor.handleBrandPacket(wrapper, settings.getBrandSettings(), player)) {
                event.markForReEncode(true);
                return;
            }
            if (player == null || !player.hasPermission("pluginshider.bypass.channels")) {
                ChannelPacketProcessor.handleChannelPacket(event, wrapper, settings.getChannelSettings());
            }
        } else if (event.getPacketType() == PacketType.Configuration.Server.PLUGIN_MESSAGE) {
            WrapperConfigServerPluginMessage wrapper = new WrapperConfigServerPluginMessage(event);
            if (BrandPacketProcessor.handleBrandPacket(wrapper, settings.getBrandSettings(), player)) {
                event.markForReEncode(true);
                return;
            }
            if (player == null || !player.hasPermission("pluginshider.bypass.channels")) {
                ChannelPacketProcessor.handleChannelPacket(event, wrapper, settings.getChannelSettings());
            }
        } else if (event.getPacketType() == PacketType.Play.Server.DECLARE_COMMANDS) {
            DeclareCommandsProcessor.filterDeclareCommands(event, player, settings);
        } else if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
            WrapperPlayServerTabComplete wrapper = new WrapperPlayServerTabComplete(event);
            String inputText = null;
            UUID uuid = player != null ? player.getUniqueId() : (event.getUser() != null ? event.getUser().getUUID() : null);
            if (uuid != null) {
                Optional<Integer> idOpt = wrapper.getTransactionId();
                if (idOpt.isPresent()) {
                    inputText = tabRequestMap.remove(uuid + ":" + idOpt.get());
                }
                if (inputText == null) {
                    inputText = lastPlayerTabMap.get(uuid);
                }
            }
            TabCompleteProcessor.filterTabComplete(event, player, settings, inputText);
        }
    }
}
