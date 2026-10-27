package io.github.lino9999.pluginshider.security;

import io.github.lino9999.pluginshider.config.SpamSettings;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandRateLimiter {
    private final Map<UUID, Deque<Long>> playerTimestamps = new ConcurrentHashMap<>();

    public boolean recordAndCheckExceeded(UUID playerUuid, SpamSettings settings) {
        if (!settings.isEnabled() || playerUuid == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        long windowMillis = settings.getWindowSeconds() * 1000L;
        long cutoff = now - windowMillis;

        Deque<Long> queue = playerTimestamps.computeIfAbsent(playerUuid, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst() < cutoff) {
                queue.pollFirst();
            }
            if (queue.size() > settings.getMaxCommands() * 2) {
                queue.pollFirst();
            }
            queue.addLast(now);
            return queue.size() > settings.getMaxCommands();
        }
    }

    public void remove(UUID playerUuid) {
        if (playerUuid != null) {
            playerTimestamps.remove(playerUuid);
        }
    }

    public void clear() {
        playerTimestamps.clear();
    }
}
