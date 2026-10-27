package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerPluginMessage;
import io.github.lino9999.pluginshider.config.BrandSettings;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class BrandPacketProcessor {
    private BrandPacketProcessor() {}

    public static boolean handleBrandPacket(WrapperCommonServerPluginMessage<?> wrapper, BrandSettings settings) {
        if (!settings.isEnabled()) {
            return false;
        }

        String channel = wrapper.getChannelName();
        if ("minecraft:brand".equalsIgnoreCase(channel) || "MC|Brand".equalsIgnoreCase(channel)) {
            byte[] encoded = encodeString(settings.getBrand());
            wrapper.setData(encoded);
            return true;
        }
        return false;
    }

    private static byte[] encodeString(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int value = bytes.length;
        while ((value & 0xFFFFFF80) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value & 0x7F);
        out.write(bytes, 0, bytes.length);
        return out.toByteArray();
    }
}
