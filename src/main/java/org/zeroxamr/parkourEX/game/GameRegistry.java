package org.zeroxamr.parkourEX.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class GameRegistry {
    private static final HashMap<Integer, GameInstance> parkourGames = new HashMap<>();
    private static final HashMap<Location, Integer> parkourGamesByLocation = new HashMap<>();

    public static void disableCollision(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("parkourCollisions");
        if (team == null) {
            team = scoreboard.registerNewTeam("parkourCollisions");
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        team.addEntry(player.getName());
    }

    public static void resetCollisionToDefault(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("parkourCollisions");
        if (team == null) {
            return;
        }
        team.removeEntry(player.getName());
    }

    public static HashMap<Integer, GameInstance> getParkourGames() {
        return parkourGames;
    }

    public static GameInstance getParkourGame(Integer id) {
        return parkourGames.get(id);
    }

    public static boolean hasGame(int gameID) {
        return parkourGames.containsKey(gameID);
    }

    public static void registerGame(int id, GameInstance game, LinkedHashMap<Location, Integer> checkpoints) {
        parkourGames.put(id, game);

        for (Location loc : checkpoints.keySet()) {
            Location strippedLocation = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            parkourGamesByLocation.put(strippedLocation, id);
        }
    }

    public static GameInstance getGameByLocation(Location location) {
        Integer id = parkourGamesByLocation.get(location);
        if (id == null) return null;

        return parkourGames.get(id);
    }

    public static void cleanup() {
        parkourGames.clear();
        parkourGamesByLocation.clear();
    }
}
