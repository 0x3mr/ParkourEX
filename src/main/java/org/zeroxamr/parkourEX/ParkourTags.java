package org.zeroxamr.parkourEX;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ParkourTags implements Listener {
    private record LocationID(String ID, int size, int index, Location location) {}
    private record ChunkCoord(World world, int x, int z) {}
    private static HashMap<ChunkCoord, List<LocationID>> checkpointsPerChunk = new HashMap<>();

    private static void build(List<LocationID> incomingLocations) {
        int size = incomingLocations.size();
        if (size <= 0) return;

        for (int i = 0; i < size; i++) {
            LocationID locationID = incomingLocations.get(i);

            Location location = locationID.location;
            World world = location.getWorld();
            String ID = locationID.ID;
            int index = locationID.index;
            int parkourSize = locationID.size;

            Location loc = location.clone();
            loc.setX(loc.getX() + 0.5);
            loc.setY(loc.getY() + 2);
            loc.setZ(loc.getZ() + 0.5);

            // Skip if a hologram already exists at this location (loaded from disk)
            boolean exists = false;
            for (ArmorStand existing : world.getEntitiesByClass(ArmorStand.class)) {
                if (Utilities.hasID(existing.getPersistentDataContainer(), "hologram") &&
                        existing.getLocation().getBlockX() == loc.getBlockX() &&
                        existing.getLocation().getBlockY() == loc.getBlockY() &&
                        existing.getLocation().getBlockZ() == loc.getBlockZ()) {
                    exists = true;
                    break;
                }
            }
            if (exists) continue;

            ArmorStand hg = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            Utilities.attachID(hg.getPersistentDataContainer(), "hologram", ID);
            hg.setMarker(true);
            hg.setVisible(false);
            hg.setGravity(false);
            hg.setPersistent(true);
            hg.setCustomNameVisible(true);

            if (index == 0) {
                hg.setCustomName("" + ChatColor.GREEN + ChatColor.BOLD + "Start");
            } else if (index == parkourSize - 1) {
                hg.setCustomName("" + ChatColor.RED + ChatColor.BOLD + "End");
            } else {
                hg.setCustomName("" + ChatColor.YELLOW + ChatColor.BOLD + "Checkpoint" + ChatColor.AQUA + ChatColor.BOLD + " #" + index);
            }
        }
    }

    public static void register(List<Location> coordinatesLocation, String ID) {
        int i = 0;
        List<LocationID> loadedChunks = new ArrayList<>();

        for (Location location : coordinatesLocation) {
            ChunkCoord chunkCoord = new ChunkCoord(
                    location.getWorld(),
                    location.getChunk().getX(),
                    location.getChunk().getZ()
            );

            LocationID locationID = new LocationID(
                    ID,
                    coordinatesLocation.size(),
                    i,
                    location
            );

            if (location.getChunk().isLoaded()) {
                loadedChunks.add(locationID);
            } else {
                checkpointsPerChunk.computeIfAbsent(chunkCoord, chunk -> new ArrayList<>()).add(locationID);
            }

            i++;
        }

        build(loadedChunks);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkCoord chunk = new ChunkCoord(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        List<LocationID> locations = checkpointsPerChunk.get(chunk);
        if (locations == null) return;
        build(locations);
    }

    public static void cleanup() {
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds) {
            for (ArmorStand hg : world.getEntitiesByClass(ArmorStand.class)) {
                if (Utilities.hasID(hg.getPersistentDataContainer(), "hologram")) {
                    hg.remove();
                }
            }
        }
    }
}