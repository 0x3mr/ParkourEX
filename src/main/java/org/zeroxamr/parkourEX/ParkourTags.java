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

    private static ArmorStand createHologram(String customName, World world,
                                             double x, double y, double z,
                                             String id, String idValue,
                                             boolean marker,boolean visibility, boolean gravity,
                                             boolean persistence, boolean showName) {
        Location location = new Location(world, x, y, z);
        ArmorStand hologram = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        Utilities.attachID(hologram.getPersistentDataContainer(), id, idValue);

        hologram.setMarker(marker);
        hologram.setGravity(gravity);
        hologram.setVisible(visibility);
        hologram.setCustomName(customName);
        hologram.setPersistent(persistence);
        hologram.setCustomNameVisible(showName);

        return hologram;
    }

    private static List<ArmorStand> build(List<LocationID> incomingLocations) {
        int size = incomingLocations.size();
        if (size == 0) return new ArrayList<>();

        List<ArmorStand> armorStands = new ArrayList<>();

        for (LocationID locationID : incomingLocations) {
            Location location = locationID.location;
            String ID = locationID.ID;
            String State = locationID.index == 0 ? "START" : locationID.index == locationID.size - 1 ? "END" : "CHECKPOINT";

            if (State.equals("START")) {
                armorStands.add(createHologram(
                    "§e§lParkour Challenge", location.getWorld(),
                    location.getX() + 0.5,
                    location.getY() + 1.5,
                    location.getZ() + 0.5,
                    "hologram", ID + ".start.0",
                    true, false, false, false, true
                ));

                armorStands.add(createHologram(
                    "§a§lStart", location.getWorld(),
                    location.getX() + 0.5,
                    location.getY() + 1.125,
                    location.getZ() + 0.5,
                    "hologram", ID + ".start.1",
                    true, false, false, false, true
                ));
            }
            else if (State.equals("CHECKPOINT")) {
                armorStands.add(createHologram(
                        "§e§lCheckpoint", location.getWorld(),
                        location.getX() + 0.5,
                        location.getY() + 1.5,
                        location.getZ() + 0.5,
                        "hologram", ID + ".checkpoint.0",
                        true, false, false, false, true
                ));

                armorStands.add(createHologram(
                        "§b§l#" + locationID.index, location.getWorld(),
                        location.getX() + 0.5,
                        location.getY() + 1.125,
                        location.getZ() + 0.5,
                        "hologram", ID + ".checkpoint.1",
                        true, false, false, false, true
                ));
            }
            else {
                armorStands.add(createHologram(
                        "§e§lParkour Challenge", location.getWorld(),
                        location.getX() + 0.5,
                        location.getY() + 1.5,
                        location.getZ() + 0.5,
                        "hologram", ID + ".end.0",
                        true, false, false, false, true
                ));

                armorStands.add(createHologram(
                        "§c§lEnd", location.getWorld(),
                        location.getX() + 0.5,
                        location.getY() + 1.125,
                        location.getZ() + 0.5,
                        "hologram", ID + ".end.1",
                        true, false, false, false, true
                ));
            }
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