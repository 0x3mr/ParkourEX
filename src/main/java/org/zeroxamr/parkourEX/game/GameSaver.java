package org.zeroxamr.parkourEX.game;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.zeroxamr.parkourEX.Main;

public class GameSaver {
    private static BukkitTask periodicFlush;

    public static void startScheduler() {
        periodicFlush = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), () -> {
            if (StatsRegistry.isDirty()) {
                StatsRegistry.clearDirtyTrack();
                Main.getDBM().flushPlayersData();
            }
        }, 0, 20L * 60); // save every minute
    }

    public static void stopScheduler() {
        if (periodicFlush != null) periodicFlush.cancel();
        periodicFlush = null;
    }
}
