package org.zeroxamr.parkourEX;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.Commands.Commands;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParkourGame implements Listener {
    private final int id;
    private final Main plugin;
    private final String gameAdmin;

    private final LinkedHashMap<Location, Integer> checkpointMap = new LinkedHashMap<>();
    private final List<Float> checkpointYaws = new ArrayList<>();

    ParkourGame(Main plugin, int id, LinkedHashMap<Location, Integer> incomingCheckpointMap, String parkourCreator, boolean buildState) {
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

        ParkourTags.register(new ArrayList<>(checkpointMap.keySet()), String.valueOf(id), buildState);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Utilities.resetPlayerInfo(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        Location oldLocation = e.getFrom();
        Location currLocation = e.getTo();

        if (oldLocation.getBlockX() == currLocation.getBlockX() &&
                oldLocation.getBlockY() == currLocation.getBlockY() &&
                oldLocation.getBlockZ() == currLocation.getBlockZ()) {
            return;
        }

        if (player.hasMetadata("inParkour") && player.hasMetadata("checkpointNumber")) {
            Location playerLocation = currLocation.getBlock().getLocation();

            if (!checkpointMap.containsKey(playerLocation)) return;

            Integer playerCheckpoint = player.getMetadata("checkpointNumber").getFirst().asInt();
            Integer parkourCheckpoint = checkpointMap.get(playerLocation);

            Location respawnLocation = playerLocation.clone();
            respawnLocation.setYaw(checkpointYaws.get(parkourCheckpoint));

            if (player.getMetadata("inParkour").getFirst().asBoolean()) {
                if (!player.hasMetadata("parkourID")) {
                    // This edge case scenario *should* never happen, but just as a safeguard
                    return;
                }

                int gameID = player.getMetadata("parkourID").getFirst().asInt();

                if (!plugin.getParkourGame(gameID).checkpointMap.containsKey(playerLocation)) {
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

                        player.sendMessage("" + ChatColor.RED + "You skipped a checkpoint! Parkour failed!");
                        playerStateCancel(player);

                        return;
                    }

                    // advance to this new parkour checkpoint

                    player.setMetadata("checkpointNumber", new FixedMetadataValue(plugin, parkourCheckpoint));
                    player.setMetadata("checkpointLocation", new FixedMetadataValue(plugin, Utilities.serializeLocation(respawnLocation)));

                    LocalTime nowTime = LocalTime.now();
                    LocalTime startTime = LocalTime.parse(player.getMetadata("startTime").getFirst().asString());
                    LocalTime oldTime = LocalTime.parse(player.getMetadata("latestCheckpointTime").getFirst().asString());

                    String time = Utilities.getDurationBetween(startTime, nowTime);
                    String diffTime = Utilities.getDurationBetween(oldTime, nowTime);
                    String bestTime = "UNKNOWN";

                    if (parkourCheckpoint + 1 == checkpointMap.size()) {
                        // Declare winner

                        playerStateCancel(player);
                        player.sendMessage("§a§lThat's a new record of §e§l" + time + "§a§l! Try again to get an even better record!");

                        return;
                    }

                    player.setMetadata("latestCheckpointTime", new FixedMetadataValue(plugin, nowTime.format(DateTimeFormatter.ISO_LOCAL_TIME)));

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
        else {
            Utilities.resetPlayerInfo(player);
        }
    }

    public void playerStateStart(Player player, int ID) {
        if (ID > Main.getParkourGames().size() || ID <= 0) {
            player.sendMessage("§cParkour not found.");
            player.sendMessage("§cEnter a valid parkour id.");
            return;
        }

        player.setMetadata("parkourID", new FixedMetadataValue(plugin, ID));
        playerStateReset(player);
    }

    public static void playerStateCheckpoint(Player player) {
        Location location = Utilities.deserializeLocation(player.getMetadata("checkpointLocation").getFirst().asString());
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);
        player.teleport(location);
    }

    public void playerStateReset(Player player) {
        Location respawnLocation = checkpointMap.firstEntry().getKey().clone();
        respawnLocation.setYaw(checkpointYaws.getFirst());

        player.setMetadata("inParkour", new FixedMetadataValue(plugin, true));
        player.setMetadata("checkpointNumber", new FixedMetadataValue(plugin, 0));
        player.setMetadata("checkpointLocation", new FixedMetadataValue(plugin, Utilities.serializeLocation(respawnLocation)));

        String playerTime = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);

        player.setMetadata("startTime", new FixedMetadataValue(plugin, playerTime));
        player.setMetadata("latestCheckpointTime", new FixedMetadataValue(plugin, playerTime));

        Services.addLastCheckpoint(player);
        Services.addResetParkour(player);
        Services.addLeaveParkour(player);
    }

    public void playerStateCancel(Player player) {
        if (Objects.equals(plugin.getConfig().get("returnToStart"), true)) {
            Location location = Main.getParkourGames().get(player.getMetadata("parkourID").getFirst().asInt()).getCheckpointMapWithYaw().firstEntry().getKey();
            location.setX(location.getX() + 0.5);
            location.setZ(location.getZ() + 0.5);

            Vector direction = location.getDirection();
            direction.setY(0).normalize().multiply(-1.5);
            location.add(direction);

            player.teleport(location);
        }

        Utilities.resetPlayerInfo(player);
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