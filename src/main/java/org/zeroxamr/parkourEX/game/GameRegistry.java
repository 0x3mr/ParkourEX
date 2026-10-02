package org.zeroxamr.parkourEX.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.game.models.CommandMeta;
import org.zeroxamr.parkourEX.util.Shared;

import java.util.*;

public class GameRegistry {
    private static final HashMap<Integer, GameInstance> parkourGames = new HashMap<>();
    private static final HashMap<Location, Integer> parkourGamesByLocation = new HashMap<>();

    private static final HashMap<Integer, List<CommandMeta>> startCommands = new HashMap<>();
    private static final HashMap<Integer, List<CommandMeta>> endCommands = new HashMap<>();
    private static final HashMap<Integer, List<CommandMeta>> exitCommands = new HashMap<>();

    public static void addExitCommand(int parkourID, CommandMeta cmd) {
        exitCommands.computeIfAbsent(parkourID, list -> new ArrayList<>()).add(cmd);
    }

    public static void addCommand(String commandType, int parkourID, CommandMeta command) {
        HashMap<Integer, List<CommandMeta>> commandsList =
                Objects.equals(commandType, "onParkourStart") ? startCommands
                        : Objects.equals(commandType, "onParkourEnd") ? endCommands
                        : Objects.equals(commandType, "onParkourExit") ? exitCommands
                        : null;

        if (commandsList == null) {
            Main.getPlugin().getLogger().info("Failed to register custom events in commands.yml");
            return;
        }

        if (parkourID == -1) {
            for (int id : parkourGames.keySet()) {
                commandsList.computeIfAbsent(id, list -> new ArrayList<>()).add(command);
            }
        }
        else {
            commandsList.computeIfAbsent(parkourID, list -> new ArrayList<>()).add(command);
        }
    }

    public static void clearCommands() {
        startCommands.clear();
        endCommands.clear();
        exitCommands.clear();
    }

    public static void executeStartCommands(int parkourID, Player player) {
        List<CommandMeta> commands = startCommands.get(parkourID);
        if (commands == null || commands.isEmpty()) return;

        for (CommandMeta cmd : commands) {
            String command = Shared.parsePlaceholders(cmd.command(), player, parkourID);

            CommandSender cmdSender = switch (cmd.executor()) {
                case CONSOLE -> Bukkit.getConsoleSender();
                case PLAYER -> player;
            };

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () ->
                            Bukkit.dispatchCommand(cmdSender, command),
                    cmd.delay()
            );

            if (cmdSender instanceof Player) {
                Main.getPlugin().getLogger().info(player.getName() + " issued server command: /" + command);
            }
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

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () ->
                    Bukkit.dispatchCommand(cmdSender, command),
                    cmd.delay()
            );

            if (cmdSender instanceof Player) {
                Main.getPlugin().getLogger().info(player.getName() + " issued server command: /" + command);
            }
        }
    }

    public static void executeFinishCommands(int parkourID, Player player) {
        List<CommandMeta> commands = endCommands.get(parkourID);
        if (commands == null || commands.isEmpty()) return;

        for (CommandMeta cmd : commands) {
            String command = Shared.parsePlaceholders(cmd.command(), player, parkourID);

            CommandSender cmdSender = switch (cmd.executor()) {
                case CONSOLE -> Bukkit.getConsoleSender();
                case PLAYER -> player;
            };

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () ->
                            Bukkit.dispatchCommand(cmdSender, command),
                    cmd.delay()
            );

            if (cmdSender instanceof Player) {
                Main.getPlugin().getLogger().info(player.getName() + " issued server command: /" + command);
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
