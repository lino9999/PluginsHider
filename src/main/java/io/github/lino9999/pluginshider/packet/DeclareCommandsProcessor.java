package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.Node;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import io.github.lino9999.pluginshider.config.PluginSettings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DeclareCommandsProcessor {
    private DeclareCommandsProcessor() {}

    public static void filterDeclareCommands(PacketSendEvent event, Player player, PluginSettings settings) {
        if (player == null || player.hasPermission("pluginshider.bypass.tab")) {
            return;
        }

        AccessGroup group = settings.getGroupManager().resolvePlayerGroup(player);
        WrapperPlayServerDeclareCommands wrapper = new WrapperPlayServerDeclareCommands(event);
        List<Node> nodes = wrapper.getNodes();
        int rootIndex = wrapper.getRootIndex();

        if (nodes == null || rootIndex < 0 || rootIndex >= nodes.size()) {
            return;
        }

        Node rootNode = nodes.get(rootIndex);
        List<Integer> children = rootNode.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }

        List<Integer> filteredChildren = new ArrayList<>();
        boolean bypassPlugins = player.hasPermission("pluginshider.bypass.plugins");

        for (Integer childIndex : children) {
            if (childIndex < 0 || childIndex >= nodes.size()) {
                continue;
            }
            Node childNode = nodes.get(childIndex);
            Optional<String> nameOpt = childNode.getName();
            if (nameOpt.isEmpty()) {
                filteredChildren.add(childIndex);
                continue;
            }

            String commandName = nameOpt.get();
            if (settings.getPluginShieldSettings().isEnabled() && !bypassPlugins) {
                if (settings.getPluginShieldSettings().isBlocked("/" + commandName)) {
                    continue;
                }
            }

            if (group.allowsRootTab(commandName)) {
                filteredChildren.add(childIndex);
            }
        }

        rootNode.setChildren(filteredChildren);
        event.markForReEncode(true);
    }
}
