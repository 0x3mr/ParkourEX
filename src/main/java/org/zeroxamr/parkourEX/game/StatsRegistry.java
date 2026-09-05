package org.zeroxamr.parkourEX.game;

import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.game.models.PlayerMeta;

import java.util.HashMap;

public class StatsRegistry {
    private static Main plugin;

    public static void initialize(Main plugin) {
        StatsRegistry.plugin = plugin;
    }

    private static HashMap<String, PlayerMeta> playerStatisticsTable = new HashMap<>();
    private static HashMap<String, Long> perGameCheckpointsTable = new HashMap<>();

    public static Long getCheckpointTime(String checkpointID) {
        return perGameCheckpointsTable.get(checkpointID);
    }

    public static void diffPlayerMeta(String playerID, long incomingValue) {
        PlayerMeta oldPlayerMeta = playerStatisticsTable.get(playerID);
        PlayerMeta newPlayerMeta = new PlayerMeta(incomingValue);

        if (oldPlayerMeta == null) {
            playerStatisticsTable.put(playerID, newPlayerMeta);
            return;
        }

        Long existingScore = oldPlayerMeta.bestScore();
        if (existingScore > incomingValue) {
            playerStatisticsTable.put(playerID, newPlayerMeta);
        }
    }

    public static void diffCheckpointStats(String checkpointID, long incomingValue) {
        Long oldCheckpoint = perGameCheckpointsTable.get(checkpointID);

        if (oldCheckpoint == null) {
            perGameCheckpointsTable.put(checkpointID, incomingValue);
            return;
        }

        if (oldCheckpoint > incomingValue) {
            perGameCheckpointsTable.put(checkpointID, incomingValue);
        }
    }

    public static Long getPlayerBestScore(String playerID) {
        PlayerMeta pm = playerStatisticsTable.get(playerID);
        if (pm == null) return null;
        return pm.bestScore();
    }

    public static HashMap<String, PlayerMeta> getPlayerStatisticsTable() {
        return playerStatisticsTable;
    }

    public static HashMap<String, Long> getPerGameCheckpointsTable() {
        return perGameCheckpointsTable;
    }
}
