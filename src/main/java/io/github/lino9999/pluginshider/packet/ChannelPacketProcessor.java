package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerPluginMessage;
import io.github.lino9999.pluginshider.config.ChannelSettings;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ChannelPacketProcessor {
    private ChannelPacketProcessor() {}

    public static boolean handleChannelPacket(PacketSendEvent event, WrapperCommonServerPluginMessage<?> wrapper, ChannelSettings settings) {
        if (!settings.isEnabled()) {
            return false;
        }

        String channel = wrapper.getChannelName();
        if (channel == null) {
            return false;
        }

        if (channel.equalsIgnoreCase("minecraft:register")
                || channel.equalsIgnoreCase("register")
                || channel.equalsIgnoreCase("minecraft:unregister")
                || channel.equalsIgnoreCase("unregister")) {

            byte[] data = wrapper.getData();
            if (data == null || data.length == 0) {
                event.setCancelled(true);
                return true;
            }

            String payload = new String(data, StandardCharsets.UTF_8);
            String[] channels = payload.split("\0");
            List<String> preserved = new ArrayList<>();

            for (String ch : channels) {
                if (settings.isChannelAllowed(ch)) {
                    preserved.add(ch);
                }
            }

            if (preserved.isEmpty()) {
                event.setCancelled(true);
            } else {
                String rebuilt = String.join("\0", preserved);
                wrapper.setData(rebuilt.getBytes(StandardCharsets.UTF_8));
                event.markForReEncode(true);
            }
            return true;
        }
        return false;
    }
}
