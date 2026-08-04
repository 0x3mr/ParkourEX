package org.zeroxamr.parkourEX.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.Services;
import org.zeroxamr.parkourEX.game.models.CommandExecutor;
import org.zeroxamr.parkourEX.game.models.CommandMeta;
import org.zeroxamr.parkourEX.util.Shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GameRegistry {
    private static Main plugin;

    public static void initialize(Main plugin) {
        GameRegistry.plugin = plugin;
    }

    private static final HashMap<Integer, GameInstance> parkourGames = new HashMap<>();
    private static final HashMap<Location, Integer> parkourGamesByLocation = new HashMap<>();

    private static final HashMap<Integer, List<CommandMeta>> exitCommands = new HashMap<>();

    public static void addExitCommand(int parkourID, CommandMeta cmd) {
        exitCommands.computeIfAbsent(parkourID, list -> new ArrayList<>()).add(cmd);
    }

    public static void addExitCommandToAll(CommandMeta cmd) {
        for (int id : parkourGames.keySet()) {
            exitCommands.computeIfAbsent(id, list -> new ArrayList<>()).add(cmd);
        }
    }

    public static void executeExitCommands(int parkourID, Player player) {
        List<CommandMeta> commands = exitCommands.get(parkourID);
        if (commands == null || commands.isEmpty()) return;

        for (CommandMeta cmd : commands) {
            String command = Shared.parsePlaceholders(cmd.command(), player, parkourID);

            CommandSender cmdSender = switch (cmd.executor()) {
                case CONSOLE -> Bukkit.getConsoleSender();
                case PLAYER -> player;
            };

            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    Bukkit.dispatchCommand(cmdSender, command),
                    cmd.delay()
            );

            if (cmdSender instanceof Player) {
                plugin.getLogger().info(player.getName() + " issued server command: /" + command);
            }
        }
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

    public static String getParkourName(int id) {
        return parkourGames.get(id).getName();
    }

    public static void cleanup() {
        parkourGames.clear();
        parkourGamesByLocation.clear();
    }
}
