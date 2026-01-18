package com.dnocturne.basalt.listener;

import com.dnocturne.basalt.util.TaskUtil;
import com.dnocturne.basalt.util.TimeUtil;
import com.dnocturne.basalt.util.TimeUtil.MoonPhase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects day/night transitions in worlds and calls callbacks.
 *
 * <p>This listener runs a periodic task that checks for time transitions
 * in all normal environment worlds (skips Nether and End).</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TimeTransitionListener listener = new TimeTransitionListener() {
 *     @Override
 *     protected void onNightfall(World world, MoonPhase moonPhase) {
 *         // Handle night starting
 *         for (Player player : world.getPlayers()) {
 *             player.sendMessage("Night has fallen!");
 *         }
 *     }
 *
 *     @Override
 *     protected void onDawn(World world) {
 *         // Handle day starting
 *         for (Player player : world.getPlayers()) {
 *             player.sendMessage("The sun rises!");
 *         }
 *     }
 * };
 *
 * listener.start();
 * // Later...
 * listener.stop();
 * }</pre>
 */
public abstract class TimeTransitionListener {

    /**
     * Default check interval in ticks (100 ticks = 5 seconds).
     */
    public static final long DEFAULT_CHECK_INTERVAL = 100L;

    private final long checkInterval;
    private final Map<String, Boolean> wasNight = new ConcurrentHashMap<>();
    private BukkitTask task;

    /**
     * Create a listener with the default check interval (5 seconds).
     */
    protected TimeTransitionListener() {
        this(DEFAULT_CHECK_INTERVAL);
    }

    /**
     * Create a listener with a custom check interval.
     *
     * @param checkIntervalTicks How often to check for transitions (in ticks)
     */
    protected TimeTransitionListener(long checkIntervalTicks) {
        this.checkInterval = checkIntervalTicks;
    }

    /**
     * Start the time checking task.
     */
    public void start() {
        if (task != null) {
            return; // Already running
        }
        task = TaskUtil.runTimer(this::checkTimeTransitions, checkInterval, checkInterval);
    }

    /**
     * Stop the time checking task and clean up resources.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        wasNight.clear();
    }

    /**
     * Check if the listener is currently running.
     *
     * @return true if the task is active
     */
    public boolean isRunning() {
        return task != null;
    }

    /**
     * Called when night begins in a world.
     *
     * @param world     The world where night just started
     * @param moonPhase The current moon phase
     */
    protected abstract void onNightfall(World world, MoonPhase moonPhase);

    /**
     * Called when day begins in a world.
     *
     * @param world The world where day just started
     */
    protected abstract void onDawn(World world);

    /**
     * Check if a world should be monitored for time transitions.
     *
     * <p>Override this to customize which worlds are tracked.
     * Default implementation skips Nether and End.</p>
     *
     * @param world The world to check
     * @return true if this world should be monitored
     */
    protected boolean shouldMonitorWorld(World world) {
        return world.getEnvironment() == World.Environment.NORMAL;
    }

    private void checkTimeTransitions() {
        // Collect currently loaded world names to detect unloaded worlds
        Set<String> loadedWorldNames = new HashSet<>();

        for (World world : Bukkit.getWorlds()) {
            if (!shouldMonitorWorld(world)) {
                continue;
            }

            String worldName = world.getName();
            loadedWorldNames.add(worldName);

            boolean isNightNow = TimeUtil.isNight(world);
            Boolean wasNightBefore = wasNight.get(worldName);

            // First check - initialize state
            if (wasNightBefore == null) {
                wasNight.put(worldName, isNightNow);
                continue;
            }

            // Transition to night
            if (isNightNow && !wasNightBefore) {
                MoonPhase phase = TimeUtil.getMoonPhaseEnum(world);
                onNightfall(world, phase);
            }
            // Transition to day
            else if (!isNightNow && wasNightBefore) {
                onDawn(world);
            }

            wasNight.put(worldName, isNightNow);
        }

        // Clean up entries for unloaded worlds to prevent memory leaks
        wasNight.keySet().removeIf(worldName -> !loadedWorldNames.contains(worldName));
    }
}
