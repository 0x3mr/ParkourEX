package org.zeroxamr.parkourEX;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class ParkourGame implements Listener {
    private String gameAdmin = "none";
    private final UUID id;
    private final Main plugin;
    private LinkedHashMap<Location, Integer> coordinates = new LinkedHashMap<>();
    private List<Location> coordinatesLocation = new ArrayList<>();
    private List<Float> coordinatesYaw = new ArrayList<>();

    ParkourGame(Main plugin, UUID id, LinkedHashMap<Location, Integer> coordinates) {
        this.plugin = plugin;
        this.id = id;

        int i = 0;
        for (Location loc : coordinates.keySet()) {
            Location locCoord = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            Float locYaw = loc.getYaw();

            this.coordinates.put(locCoord, i);
            coordinatesLocation.add(locCoord);
            coordinatesYaw.add(locYaw);

            i++;
        }

        ParkourTags.build(coordinatesLocation, id.toString());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Utilities.resetPlayerInfo(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location oldLocation = e.getFrom();
        Location playerLocation = e.getTo();

        if (oldLocation.getBlockX() == playerLocation.getBlockX() &&
            oldLocation.getBlockY() == playerLocation.getBlockY() &&
            oldLocation.getBlockZ() == playerLocation.getBlockZ()) {
            return;
        }

        if (player.hasMetadata("inParkour") && player.hasMetadata("checkpoint")) {
            Location playerL = playerLocation.getBlock().getLocation();

            if (!coordinates.containsKey(playerL)) return;

            Integer checkpoint = player.getMetadata("checkpoint").getFirst().asInt();
            Integer blockCheckpoint = coordinates.get(playerL);

            Location savedLoc = playerL.clone();
            savedLoc.setYaw(coordinatesYaw.get(blockCheckpoint));

            if (player.getMetadata("inParkour").getFirst().asBoolean()) {
                if (!player.hasMetadata("parkourID")) {
//                    plugin.getLogger().info(player.getName() + " is not in a parkour!");
                    return;
                }

                UUID gameID = UUID.fromString(player.getMetadata("parkourID").getFirst().asString());
                if (!plugin.getParkourGame(gameID).coordinatesLocation.contains(playerL)) {
//                    plugin.getLogger().info(player.getName() + " interfered with another parkour!");
                    return;
                }

                if (playerL.equals(coordinatesLocation.getFirst())) {
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Reset your timer to 00:00! Get to the finish line!");
                    player.setMetadata("inParkour", new FixedMetadataValue(plugin, true));
                    player.setMetadata("checkpoint", new FixedMetadataValue(plugin, 0));
                    player.setMetadata("lastCheckpoint", new FixedMetadataValue(plugin, Utilities.serializeLocation(savedLoc)));
                    String playerTime = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
                    player.setMetadata("timer", new FixedMetadataValue(plugin, playerTime));
                    player.setMetadata("checkpointTimer", new FixedMetadataValue(plugin, playerTime));
                }
                else if (checkpoint.equals(blockCheckpoint)) {
                    // Do nothing
                } else if (checkpoint + 1 == blockCheckpoint) {
                    // advance to this checkpoint

                    player.setMetadata("checkpoint", new FixedMetadataValue(plugin, blockCheckpoint));
                    player.setMetadata("lastCheckpoint", new FixedMetadataValue(plugin, Utilities.serializeLocation(savedLoc)));

                    LocalTime nowTime = LocalTime.now();
                    LocalTime startTime = LocalTime.parse(player.getMetadata("timer").getFirst().asString());
                    LocalTime oldTime = LocalTime.parse(player.getMetadata("checkpointTimer").getFirst().asString());

                    String time = Utilities.getDurationBetween(startTime, nowTime);
                    String diffTime = Utilities.getDurationBetween(oldTime, nowTime);
                    String bestTime = "UNKNOWN";

                    if (blockCheckpoint + 1 == coordinates.size()) {
                        Utilities.resetPlayerInfo(player);
                        player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "That's a new record of " + ChatColor.YELLOW + ChatColor.BOLD + time + ChatColor.GREEN + ChatColor.BOLD + "! Try again to get an even better record!");
                        player.removeMetadata("parkourID", plugin);
                        Services.removeLastCheckpoint(player);
                        return;
                    }

                    player.setMetadata("checkpointTimer", new FixedMetadataValue(plugin, nowTime.format(DateTimeFormatter.ISO_LOCAL_TIME)));

                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "You reached " + ChatColor.YELLOW + ChatColor.BOLD + "Checkpoint #" + blockCheckpoint + ChatColor.GREEN + ChatColor.BOLD + " after " + ChatColor.YELLOW + ChatColor.BOLD + time + ChatColor.GREEN + ChatColor.BOLD + ".");
                    player.sendMessage("" + ChatColor.GRAY + "You finished this part of the parkour in " + diffTime + ".");
//                    player.sendMessage("" + ChatColor.GRAY + "You finished this part of the parkour in " + diffTime + " (personal best: " + bestTime + ").");
                } else if (checkpoint < blockCheckpoint) {
                    // skipped a checkpoint!
                    Utilities.resetPlayerInfo(player);
                    player.sendMessage("" + ChatColor.RED + "You skipped a checkpoint! Parkour failed!");
                    player.removeMetadata("parkourID", plugin);
                    Services.removeLastCheckpoint(player);
                } else if (checkpoint > blockCheckpoint) {
                    // Do nothing
                }
            } else {
                if (playerL.equals(coordinatesLocation.getFirst())) {
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Parkour challenge started!");
                    player.setMetadata("inParkour", new FixedMetadataValue(plugin, true));
                    player.setMetadata("checkpoint", new FixedMetadataValue(plugin, 0));
                    player.setMetadata("lastCheckpoint", new FixedMetadataValue(plugin, Utilities.serializeLocation(savedLoc)));
                    player.setMetadata("parkourID", new FixedMetadataValue(plugin, id));

                    LocalTime localTime = LocalTime.now();
                    String playerTime = localTime.format(DateTimeFormatter.ISO_LOCAL_TIME);

                    player.setMetadata("timer", new FixedMetadataValue(plugin, playerTime));
                    player.setMetadata("checkpointTimer", new FixedMetadataValue(plugin, playerTime));

                    Services.giveLastCheckpoint(player);
                } else if (playerL.equals(coordinatesLocation.getLast())) {
                    // let this be sent to finish line only
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "This is the finish line for the parkour! Get to the start line and climb back up here!");
                }
            }
        } else {
            Utilities.resetPlayerInfo(player);
        }
    }

    public LinkedHashMap<Location, Integer> getCoordinates() {
        return coordinates;
    }

    public List<Location> getCoordinatesLocation() {
        return coordinatesLocation;
    }

    public String getGameAdmin() {
        return gameAdmin;
    }
}