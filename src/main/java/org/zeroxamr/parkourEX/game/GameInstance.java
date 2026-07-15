package org.zeroxamr.parkourEX.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.Services;
import org.zeroxamr.parkourEX.commands.Commands;
import org.zeroxamr.parkourEX.util.Pdc;
import org.zeroxamr.parkourEX.util.Shared;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameInstance {
    private final int id;
    private final Main plugin;
    private final String gameAdmin;

    private final LinkedHashMap<Location, Integer> checkpointMap = new LinkedHashMap<>();
    private final List<Float> checkpointYaws = new ArrayList<>();

    public GameInstance(Main plugin, int id, LinkedHashMap<Location, Integer> incomingCheckpointMap, String parkourCreator) {
        this.id = id;
        this.plugin = plugin;
        gameAdmin = parkourCreator;

        int i = 0;
        for (Location loc : incomingCheckpointMap.keySet()) {
            Location location = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());

            checkpointMap.put(location, i);
            checkpointYaws.add(loc.getYaw());

            i++;
        }
    }

    public void handleParkour(Player player, Location playerLocation) {
        // Safety net: keeps checkpointMap and GameRegistry's location index in sync
        // TODO: revisit on parkour delete/edit
        if (!checkpointMap.containsKey(playerLocation)) return;

        Integer playerCheckpoint = Pdc.getInt(player, "checkpointNumber");
        Integer parkourCheckpoint = checkpointMap.get(playerLocation);

        Location respawnLocation = playerLocation.clone();
        respawnLocation.setYaw(checkpointYaws.get(parkourCheckpoint));

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))) {
            int gameID = Pdc.getInt(player, "parkourID");

            if (!GameRegistry.hasGame(gameID)) {
                player.sendMessage("§cYou're playing an invalid parkour session.");
                playerStateCancel(player);
                return;
            }

            if (!GameRegistry.getParkourGame(gameID).checkpointMap.containsKey(playerLocation)) {
//                  // If interfered with another parkour, do nothing
                return;
            }

            if (playerLocation.equals(checkpointMap.firstEntry().getKey())) {
                player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Reset your timer to 00:00! Get to the finish line!");
                playerStateReset(player);
            }
            else if (playerCheckpoint.equals(parkourCheckpoint)) {
                // If entered the same checkpoint multiple times, do nothing
            }
            else if (playerCheckpoint < parkourCheckpoint) {
                // If reached next checkpoint OR skipped a checkpoint

                if (Objects.equals(plugin.getConfig().get("skipCheckpoints"), false)
                        && playerCheckpoint + 1 < parkourCheckpoint) {
                    // skipped a checkpoint!

                    player.sendMessage("§c§lParkour challenge failed! You skipped a checkpoint!!");
                    playerStateCancel(player);

                    return;
                }

                // advance to this new parkour checkpoint

                Pdc.set(player, "checkpointNumber", parkourCheckpoint);
                Pdc.set(player, "checkpointLocation", Shared.serializeLocation(respawnLocation));

                LocalTime nowTime = LocalTime.now();
                LocalTime startTime = LocalTime.parse(Pdc.getString(player, "startTime"));
                LocalTime oldTime = LocalTime.parse(Pdc.getString(player, "latestCheckpointTime"));

                String time = Shared.getDurationBetween(startTime, nowTime);
                String diffTime = Shared.getDurationBetween(oldTime, nowTime);
                String bestTime = "UNKNOWN";

                if (parkourCheckpoint + 1 == checkpointMap.size()) {
                    // Declare winner

                    playerStateCancel(player);
                    player.sendMessage("§a§lThat's a new record of §e§l" + time + "§a§l! Try again to get an even better record!");

                    return;
                }

                Pdc.set(player, "latestCheckpointTime", nowTime.format(DateTimeFormatter.ISO_LOCAL_TIME));

                player.sendMessage("§a§lYou reached §e§lCheckpoint #" + parkourCheckpoint + "§a§l after §e§l" + time + "§a§l.");
                player.sendMessage("§7You finished this part of the parkour in §6" + diffTime + "§7.");
//                    player.sendMessage("" + ChatColor.GRAY + "You finished this part of the parkour in " + diffTime + " (personal best: " + bestTime + ").");
            }
            else if (playerCheckpoint > parkourCheckpoint) {
                // If went back to an older checkpoint, do nothing
            }
        }
        else {
            if (playerLocation.equals(checkpointMap.firstEntry().getKey())) {
                player.sendMessage("§a§lParkour challenge started!");
                player.sendMessage("§aUse §e" + Commands.getCommands().get("Checkpoint".toLowerCase()).getUsage() + " §ato teleport to the last checkpoint or §e" + Commands.getCommands().get("Cancel".toLowerCase()).getUsage() + " §ato cancel!");

                playerStateStart(player, id);
            }
            else if (playerLocation.equals(checkpointMap.lastEntry().getKey())) {
                player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "This is the finish line for the parkour! Get to the start line and climb back up here!");
            }
        }
    }

    public void playerStateStart(Player player, int ID) {
        Pdc.set(player, "parkourID", ID);

        if (Objects.equals(plugin.getConfig().get("clearAllEffects"), true)) {
            player.clearActivePotionEffects();
        }

        if (Objects.equals(plugin.getConfig().get("disableCollisions"), true)) {
            GameRegistry.disableCollision(player);
        }

        playerStateReset(player);
    }

    public static void playerStateCheckpoint(Player player) {
        Location location = Shared.deserializeLocation(Pdc.getString(player, "checkpointLocation"));
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);
        player.teleport(location);
    }

    public void playerStateReset(Player player) {
        Location respawnLocation = checkpointMap.firstEntry().getKey().clone();
        respawnLocation.setYaw(checkpointYaws.getFirst());

        Pdc.set(player, "inParkour", true);
        Pdc.set(player, "checkpointNumber", 0);
        Pdc.set(player, "checkpointLocation", Shared.serializeLocation(respawnLocation));

        String playerTime = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);

        Pdc.set(player, "startTime", playerTime);
        Pdc.set(player, "latestCheckpointTime", playerTime);

        Services.addLastCheckpoint(player);
        Services.addResetParkour(player);
        Services.addLeaveParkour(player);
    }

    public void playerStateCancel(Player player) {
        if (Objects.equals(plugin.getConfig().get("returnToStart"), true)) {
            Integer gameID = Pdc.getInt(player, "parkourID");

            Location location = GameRegistry.getParkourGame(gameID).getCheckpointMapWithYaw().firstEntry().getKey();
            location.setX(location.getX() + 0.5);
            location.setZ(location.getZ() + 0.5);

            Vector direction = location.getDirection();
            direction.setY(0).normalize().multiply(-1.5);
            location.add(direction);

            player.teleport(location);
        }

        Shared.resetPlayerInfo(player);
    }

    public LinkedHashMap<Location, Integer> getCheckpointMap() {
        return checkpointMap;
    }

    public LinkedHashMap<Location, Integer> getCheckpointMapWithYaw() {
        int i = 0;
        LinkedHashMap<Location, Integer> checkpointMapWithYaw = new LinkedHashMap<>();
        for (Map.Entry<Location, Integer> entry : checkpointMap.entrySet()) {
            Location location = entry.getKey().clone();
            location.setYaw(checkpointYaws.get(i));
            location.setPitch(0);
            checkpointMapWithYaw.put(location, entry.getValue());
            i++;
        }
        return checkpointMapWithYaw;
    }
}