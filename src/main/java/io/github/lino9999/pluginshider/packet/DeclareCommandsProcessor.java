package io.github.lino9999.pluginshider.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.Node;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import io.github.lino9999.pluginshider.acl.AccessGroup;
import io.github.lino9999.pluginshider.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DeclareCommandsProcessor {
    private DeclareCommandsProcessor() {}

    public static void filterDeclareCommands(PacketSendEvent event, Player player, PluginSettings settings) {
        if (player == null && event.getUser() != null && event.getUser().getUUID() != null) {
            player = Bukkit.getPlayer(event.getUser().getUUID());
        }

        if (player == null || player.hasPermission("pluginshider.bypass.tab")) {
            return;
        }

        AccessGroup group = settings.getGroupManager().resolvePlayerGroup(player);
        WrapperPlayServerDeclareCommands wrapper = new WrapperPlayServerDeclareCommands(event);
        List<Node> originalNodes = wrapper.getNodes();
        int rootIndex = wrapper.getRootIndex();

        if (originalNodes == null || rootIndex < 0 || rootIndex >= originalNodes.size()) {
            return;
        }

        Node rootNode = originalNodes.get(rootIndex);
        List<Integer> rootChildren = rootNode.getChildren();
        if (rootChildren == null) {
            return;
        }

        boolean bypassPlugins = player.hasPermission("pluginshider.bypass.plugins");
        boolean hideColon = settings.isHideColonCommands();

        Set<Integer> allowedRootChildrenIndices = new HashSet<>();
        for (Integer childIndex : rootChildren) {
            if (childIndex < 0 || childIndex >= originalNodes.size()) {
                continue;
            }
            Node childNode = originalNodes.get(childIndex);
            Optional<String> nameOpt = childNode.getName();
            if (nameOpt.isEmpty()) {
                allowedRootChildrenIndices.add(childIndex);
                continue;
            }

            String commandName = nameOpt.get();
            if (settings.getPluginShieldSettings().isEnabled() && !bypassPlugins) {
                if (settings.getPluginShieldSettings().isBlocked("/" + commandName)) {
                    continue;
                }
            }

            if (group.allowsRootTab(commandName, hideColon)) {
                allowedRootChildrenIndices.add(childIndex);
            }
        }

        List<Node> newNodes = new ArrayList<>();
        Map<Integer, Integer> oldToNew = new HashMap<>();

        newNodes.add(rootNode);
        oldToNew.put(rootIndex, 0);

        Deque<Integer> queue = new ArrayDeque<>(allowedRootChildrenIndices);
        while (!queue.isEmpty()) {
            int oldIdx = queue.poll();
            if (oldIdx < 0 || oldIdx >= originalNodes.size() || oldToNew.containsKey(oldIdx)) {
                continue;
            }

            Node node = originalNodes.get(oldIdx);
            int newIdx = newNodes.size();
            newNodes.add(node);
            oldToNew.put(oldIdx, newIdx);

            if (node.getChildren() != null) {
                for (int childIdx : node.getChildren()) {
                    if (!oldToNew.containsKey(childIdx)) {
                        queue.add(childIdx);
                    }
                }
            }
        }

        for (int i = 0; i < newNodes.size(); i++) {
            Node node = newNodes.get(i);
            if (i == 0) {
                List<Integer> remappedRootChildren = new ArrayList<>();
                for (int childIdx : allowedRootChildrenIndices) {
                    Integer remapped = oldToNew.get(childIdx);
                    if (remapped != null) {
                        remappedRootChildren.add(remapped);
                    }
                }
                node.setChildren(remappedRootChildren);
            } else if (node.getChildren() != null) {
                List<Integer> remappedChildren = new ArrayList<>();
                for (int childIdx : node.getChildren()) {
                    Integer remapped = oldToNew.get(childIdx);
                    if (remapped != null) {
                        remappedChildren.add(remapped);
                    }
                }
                node.setChildren(remappedChildren);
            }

            if ((node.getFlags() & Node.FLAG_REDIRECT) != 0) {
                Integer remappedRedirect = oldToNew.get(node.getRedirectNodeIndex());
                if (remappedRedirect != null) {
                    node.setRedirectNodeIndex(remappedRedirect);
                } else {
                    node.setFlags((byte) (node.getFlags() & ~Node.FLAG_REDIRECT));
                }
            }
        }

        wrapper.setRootIndex(0);
        wrapper.setNodes(newNodes);
        event.markForReEncode(true);
    }
}
