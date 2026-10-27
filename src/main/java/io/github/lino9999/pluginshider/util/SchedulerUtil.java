package io.github.lino9999.pluginshider.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SchedulerUtil {
    private static final boolean IS_FOLIA;
    private static MethodHandle GET_ASYNC_SCHEDULER;
    private static MethodHandle ASYNC_RUN_NOW;
    private static MethodHandle ASYNC_RUN_AT_FIXED_RATE;
    private static MethodHandle GET_GLOBAL_REGION_SCHEDULER;
    private static MethodHandle GLOBAL_RUN;
    private static MethodHandle PLAYER_GET_SCHEDULER;
    private static MethodHandle ENTITY_SCHEDULER_RUN;
    private static MethodHandle TASK_CANCEL;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            Class<?> bukkitClass = Bukkit.class;
            Class<?> asyncSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Class<?> globalSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> taskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

            GET_ASYNC_SCHEDULER = lookup.findStatic(bukkitClass, "getAsyncScheduler", MethodType.methodType(asyncSchedulerClass));
            ASYNC_RUN_NOW = lookup.findVirtual(asyncSchedulerClass, "runNow", MethodType.methodType(taskClass, Plugin.class, Consumer.class));
            ASYNC_RUN_AT_FIXED_RATE = lookup.findVirtual(asyncSchedulerClass, "runAtFixedRate", MethodType.methodType(taskClass, Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class));

            GET_GLOBAL_REGION_SCHEDULER = lookup.findStatic(bukkitClass, "getGlobalRegionScheduler", MethodType.methodType(globalSchedulerClass));
            GLOBAL_RUN = lookup.findVirtual(globalSchedulerClass, "run", MethodType.methodType(taskClass, Plugin.class, Consumer.class));

            PLAYER_GET_SCHEDULER = lookup.findVirtual(Player.class, "getScheduler", MethodType.methodType(entitySchedulerClass));
            ENTITY_SCHEDULER_RUN = lookup.findVirtual(entitySchedulerClass, "run", MethodType.methodType(taskClass, Plugin.class, Consumer.class, Runnable.class));

            Class<?> cancelledStateClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask$CancelledState");
            TASK_CANCEL = lookup.findVirtual(taskClass, "cancel", MethodType.methodType(cancelledStateClass));
            folia = true;
        } catch (Throwable t) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    private SchedulerUtil() {}

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA && GET_ASYNC_SCHEDULER != null) {
            try {
                Object asyncScheduler = GET_ASYNC_SCHEDULER.invoke();
                Consumer<Object> consumer = scheduledTask -> task.run();
                ASYNC_RUN_NOW.invoke(asyncScheduler, plugin, consumer);
                return;
            } catch (Throwable ignored) {}
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static Runnable runAsyncRepeating(Plugin plugin, Runnable task, long delaySeconds, long intervalSeconds) {
        if (IS_FOLIA && GET_ASYNC_SCHEDULER != null) {
            try {
                Object asyncScheduler = GET_ASYNC_SCHEDULER.invoke();
                Consumer<Object> consumer = scheduledTask -> task.run();
                Object scheduledTask = ASYNC_RUN_AT_FIXED_RATE.invoke(asyncScheduler, plugin, consumer, delaySeconds, intervalSeconds, TimeUnit.SECONDS);
                return () -> {
                    if (scheduledTask != null && TASK_CANCEL != null) {
                        try {
                            TASK_CANCEL.invoke(scheduledTask);
                        } catch (Throwable ignored) {}
                    }
                };
            } catch (Throwable ignored) {}
        }
        BukkitTask bTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                task,
                delaySeconds * 20L,
                intervalSeconds * 20L
        );
        return bTask::cancel;
    }

    public static void runOnPlayer(Plugin plugin, Player player, Consumer<Player> action) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (IS_FOLIA && PLAYER_GET_SCHEDULER != null) {
            try {
                Object entityScheduler = PLAYER_GET_SCHEDULER.invoke(player);
                Consumer<Object> consumer = task -> action.accept(player);
                ENTITY_SCHEDULER_RUN.invoke(entityScheduler, plugin, consumer, (Runnable) null);
                return;
            } catch (Throwable ignored) {}
        }
        Bukkit.getScheduler().runTask(plugin, () -> action.accept(player));
    }

    public static void runGlobalSync(Plugin plugin, Runnable task) {
        if (IS_FOLIA && GET_GLOBAL_REGION_SCHEDULER != null) {
            try {
                Object globalScheduler = GET_GLOBAL_REGION_SCHEDULER.invoke();
                Consumer<Object> consumer = scheduledTask -> task.run();
                GLOBAL_RUN.invoke(globalScheduler, plugin, consumer);
                return;
            } catch (Throwable ignored) {}
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
