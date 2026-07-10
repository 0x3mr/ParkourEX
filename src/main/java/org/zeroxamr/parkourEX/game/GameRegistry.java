package org.zeroxamr.parkourEX.game;

import org.bukkit.Location;

import java.util.HashMap;

public class GameRegistry {
    private static final HashMap<Integer, GameInstance> parkourGames = new HashMap<>();
    private static final HashMap<Location, Integer> parkourGamesByLocation = new HashMap<>();

    public static HashMap<Integer, GameInstance> getParkourGames() {
        return parkourGames;
    }

    public static GameInstance getParkourGame(Integer id) {
        return parkourGames.get(id);
    }

    public static boolean hasGame(int gameID) {
        return parkourGames.containsKey(gameID);
    }

    public static void addGame(int id, GameInstance game) {
        parkourGames.put(id, game);
    }

    public static void addGameByLocation(Location loc, int id) {
        Location strippedLocation = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        parkourGamesByLocation.put(strippedLocation, id);
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
