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
import java.util.concurrent.ConcurrentHashMap;

public final class PacketEventListener extends PacketListenerAbstract {
    private final PluginsHiderPlugin plugin;
    private final Map<Integer, String> tabRequestMap = new ConcurrentHashMap<>();

    public PacketEventListener(PluginsHiderPlugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
            Optional<Integer> idOpt = wrapper.getTransactionId();
            if (idOpt.isPresent()) {
                if (tabRequestMap.size() > 5000) {
                    tabRequestMap.clear();
                }
                tabRequestMap.put(idOpt.get(), wrapper.getText());
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
            if (BrandPacketProcessor.handleBrandPacket(wrapper, settings.getBrandSettings())) {
                event.markForReEncode(true);
                return;
            }
            if (player == null || !player.hasPermission("pluginshider.bypass.channels")) {
                ChannelPacketProcessor.handleChannelPacket(event, wrapper, settings.getChannelSettings());
            }
        } else if (event.getPacketType() == PacketType.Configuration.Server.PLUGIN_MESSAGE) {
            WrapperConfigServerPluginMessage wrapper = new WrapperConfigServerPluginMessage(event);
            if (BrandPacketProcessor.handleBrandPacket(wrapper, settings.getBrandSettings())) {
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
            Optional<Integer> idOpt = wrapper.getTransactionId();
            if (idOpt.isPresent()) {
                inputText = tabRequestMap.remove(idOpt.get());
            }
            TabCompleteProcessor.filterTabComplete(event, player, settings, inputText);
        }
    }
}
