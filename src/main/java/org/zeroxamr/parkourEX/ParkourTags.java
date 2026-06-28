package org.zeroxamr.parkourEX;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.*;

public class ParkourTags implements Listener {
    private record LocationID(String ID, int size, int index, Location location) {}
    private record ChunkCoord(World world, int x, int z) {}

    private static final HashMap<ChunkCoord, List<LocationID>> checkpointsPerChunk = new HashMap<>();
    private static final HashMap<ChunkCoord, List<ArmorStand>> checkpointsBuilt = new HashMap<>();

    private static List<ArmorStand> build(List<LocationID> incomingLocations) {
        int size = incomingLocations.size();
        if (size == 0) return new ArrayList<>();

        List<ArmorStand> armorStands = new ArrayList<>();

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

            ArmorStand hg = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            Utilities.attachID(hg.getPersistentDataContainer(), "hologram", ID);
            hg.setMarker(true);
            hg.setVisible(false);
            hg.setGravity(false);
            hg.setPersistent(false);
            hg.setCustomNameVisible(true);

            if (index == 0) {
                hg.setCustomName("" + ChatColor.GREEN + ChatColor.BOLD + "Start");
            } else if (index == parkourSize - 1) {
                hg.setCustomName("" + ChatColor.RED + ChatColor.BOLD + "End");
            } else {
                hg.setCustomName("" + ChatColor.YELLOW + ChatColor.BOLD + "Checkpoint" + ChatColor.AQUA + ChatColor.BOLD + " #" + index);
            }

            armorStands.add(hg);
        }

        return (armorStands);
    }

    public static void register(List<Location> coordinatesLocation, String ID, boolean buildState) {
        int i = 0;
        Set<ChunkCoord> addedChunks = new HashSet<>();

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

            checkpointsPerChunk.computeIfAbsent(chunkCoord, chunk -> new ArrayList<>()).add(locationID);
            if (buildState) addedChunks.add(chunkCoord);
            i++;
        }

        if (buildState) {
            loadChunkTags(addedChunks);
        }
    }

    private static void loadChunkTags(Set<ChunkCoord> affectedChunks) {
        for (ChunkCoord chunk : affectedChunks) {
            World world = chunk.world;

            if (!world.isChunkLoaded(chunk.x, chunk.z)) continue;
            if (checkpointsBuilt.containsKey(chunk)) {
                for (Entity entity : world.getChunkAt(chunk.x, chunk.z).getEntities()) {
                    if (!(entity instanceof ArmorStand)) continue;
                    if (Utilities.hasID(entity.getPersistentDataContainer(), "hologram")) {
                        entity.remove();
                    }
                }

                checkpointsBuilt.remove(chunk);
            }

            List<LocationID> locations = checkpointsPerChunk.get(chunk);
            List<ArmorStand> armorStands = build(locations);

            if (armorStands.isEmpty()) continue;

            checkpointsBuilt.put(chunk, armorStands);
        }
    }

    public static void loadTags() {
        for (ChunkCoord chunk : checkpointsPerChunk.keySet()) {
            World world = chunk.world;

            if (!world.isChunkLoaded(chunk.x, chunk.z)) continue;
            if (checkpointsBuilt.containsKey(chunk)) continue;

            List<LocationID> locations = checkpointsPerChunk.get(chunk);
            List<ArmorStand> armorStands = build(locations);

            if (armorStands.isEmpty()) continue;

            checkpointsBuilt.put(chunk, armorStands);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkCoord chunk = new ChunkCoord(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());

        if (checkpointsBuilt.containsKey(chunk)) return;
        List<LocationID> locations = checkpointsPerChunk.get(chunk);

        if (locations == null) return;
        List<ArmorStand> armorStands = build(locations);

        if (!armorStands.isEmpty()) {
            checkpointsBuilt.put(chunk, armorStands);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkCoord chunk = new ChunkCoord(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        checkpointsBuilt.remove(chunk);
    }

    public static void cleanup() {
        checkpointsBuilt.clear();
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