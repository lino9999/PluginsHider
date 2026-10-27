package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import io.github.lino9999.pluginshider.PluginsHiderPlugin;
import io.github.lino9999.pluginshider.config.PluginSettings;
import org.bukkit.entity.Player;

public final class PacketEventListener extends PacketListenerAbstract {
    private final PluginsHiderPlugin plugin;

    public PacketEventListener(PluginsHiderPlugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PluginSettings settings = plugin.getSettings();
        if (settings == null) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.PLUGIN_MESSAGE) {
            WrapperPlayServerPluginMessage wrapper = new WrapperPlayServerPluginMessage(event);
            if (BrandPacketProcessor.handleBrandPacket(wrapper, settings.getBrandSettings())) {
                event.markForReEncode(true);
                return;
            }
            Player player = (Player) event.getPlayer();
            if (player == null || !player.hasPermission("pluginshider.bypass.channels")) {
                ChannelPacketProcessor.handleChannelPacket(event, wrapper, settings.getChannelSettings());
            }
        } else if (event.getPacketType() == PacketType.Configuration.Server.PLUGIN_MESSAGE) {
            WrapperConfigServerPluginMessage wrapper = new WrapperConfigServerPluginMessage(event);
            if (BrandPacketProcessor.handleBrandPacket(wrapper, settings.getBrandSettings())) {
                event.markForReEncode(true);
                return;
            }
            Player player = (Player) event.getPlayer();
            if (player == null || !player.hasPermission("pluginshider.bypass.channels")) {
                ChannelPacketProcessor.handleChannelPacket(event, wrapper, settings.getChannelSettings());
            }
        } else if (event.getPacketType() == PacketType.Play.Server.DECLARE_COMMANDS) {
            Player player = (Player) event.getPlayer();
            DeclareCommandsProcessor.filterDeclareCommands(event, player, settings);
        } else if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
            Player player = (Player) event.getPlayer();
            TabCompleteProcessor.filterTabComplete(event, player, settings);
        }
    }
}
