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
    private final UUID id;
    private final Main plugin;
    private String gameAdmin = "none";

    private LinkedHashMap<Location, Integer> checkpointMap = new LinkedHashMap<>();
    private List<Float> checkpointYaws = new ArrayList<>();

    ParkourGame(Main plugin, UUID id, LinkedHashMap<Location, Integer> incomingCheckpointMap) {
        this.plugin = plugin;
        this.id = id;

        int i = 0;
        for (Location loc : incomingCheckpointMap.keySet()) {
            Location location = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            Float locYaw = loc.getYaw();

            checkpointMap.put(location, i);
            checkpointYaws.add(locYaw);

            i++;
        }

        ParkourTags.build(new ArrayList<Location>(checkpointMap.keySet()), id.toString());
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

                UUID gameID = UUID.fromString(player.getMetadata("parkourID").getFirst().asString());

                if (!plugin.getParkourGame(gameID).checkpointMap.containsKey(playerLocation)) {
//                  // If interfered with another parkour, do nothing
                    return;
                }


                if (playerLocation.equals(checkpointMap.firstEntry().getKey())) {
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Reset your timer to 00:00! Get to the finish line!");
                    setPlayerState(player);
                }
                else if (playerCheckpoint.equals(parkourCheckpoint)) {
                    // If entered the same checkpoint multiple times, do nothing
                }
                else if (playerCheckpoint + 1 == parkourCheckpoint) {
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
                        Utilities.resetPlayerInfo(player);
                        player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "That's a new record of " + ChatColor.YELLOW + ChatColor.BOLD + time + ChatColor.GREEN + ChatColor.BOLD + "! Try again to get an even better record!");
                        player.removeMetadata("parkourID", plugin);
                        Services.removeLastCheckpoint(player);
                        Services.removeResetParkour(player);
                        return;
                    }

                    player.setMetadata("latestCheckpointTime", new FixedMetadataValue(plugin, nowTime.format(DateTimeFormatter.ISO_LOCAL_TIME)));

                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "You reached " + ChatColor.YELLOW + ChatColor.BOLD + "Checkpoint #" + parkourCheckpoint + ChatColor.GREEN + ChatColor.BOLD + " after " + ChatColor.YELLOW + ChatColor.BOLD + time + ChatColor.GREEN + ChatColor.BOLD + ".");
                    player.sendMessage("" + ChatColor.GRAY + "You finished this part of the parkour in " + diffTime + ".");
//                    player.sendMessage("" + ChatColor.GRAY + "You finished this part of the parkour in " + diffTime + " (personal best: " + bestTime + ").");
                }
                else if (playerCheckpoint < parkourCheckpoint) {
                    // skipped a checkpoint!
                    Utilities.resetPlayerInfo(player);
                    player.sendMessage("" + ChatColor.RED + "You skipped a checkpoint! Parkour failed!");
                    player.removeMetadata("parkourID", plugin);
                    Services.removeLastCheckpoint(player);
                    Services.removeResetParkour(player);
                }
                else if (playerCheckpoint > parkourCheckpoint) {
                    // If went back to an older checkpoint, do nothing
                }
            } else {
                if (playerLocation.equals(checkpointMap.firstEntry().getKey())) {
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Parkour challenge started!");
                    player.setMetadata("parkourID", new FixedMetadataValue(plugin, id));
                    setPlayerState(player);
                }
                else if (playerLocation.equals(checkpointMap.lastEntry().getKey())) {
                    player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "This is the finish line for the parkour! Get to the start line and climb back up here!");
                }
            }
        } else {
            Utilities.resetPlayerInfo(player);
        }
    }

    public void setPlayerState(Player player) {
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
    }

    public LinkedHashMap<Location, Integer> getCheckpointMap() {
        return checkpointMap;
    }

    public String getGameAdmin() {
        return gameAdmin;
    }
}