package org.zeroxamr.parkourEX;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ParkourTags implements Listener {
    private record LocationMeta(String ID, int size, int index, Location location) {}
    private record ChunkAddress(World world, int x, int z) {}

    private static final HashMap<ChunkAddress, List<LocationMeta>> hologramsPerChunk = new HashMap<>();
    private static final Set<ArmorStand> hologramsBuilt = new HashSet<>();
    // Saved holograms are not utilized yet.
    // hologramsBuilt is out of sync on deletions
    // however, there is no deletion scenarios atm
    //
    // TODO: sync on deletion when delete option is implemented

    @NonNull
    private static ArmorStand createHologram(String customName, World world,
                                                      double x, double y, double z,
                                                      String id, String idValue,
                                                      boolean marker, boolean visibility, boolean gravity,
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

    private static List<ArmorStand> build(List<LocationMeta> incomingLocations) {
        int size = incomingLocations.size();
        if (size == 0) return new ArrayList<>();

        List<ArmorStand> armorStands = new ArrayList<>();

        for (LocationMeta locationMeta : incomingLocations) {
            Location location = locationMeta.location;
            String ID = locationMeta.ID;
            String State = locationMeta.index == 0 ? "START" : locationMeta.index == locationMeta.size - 1 ? "END" : "CHECKPOINT";

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
                        "§b§l#" + locationMeta.index, location.getWorld(),
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

        return armorStands;
    }

    public static void register(List<Location> coordinatesLocation, String ID, boolean buildState) {
        int i = 0;

        Set<ChunkAddress> addedChunks = new HashSet<>();

        for (Location location : coordinatesLocation) {
            ChunkAddress chunkAddress = new ChunkAddress(
                    location.getWorld(),
                    location.getChunk().getX(),
                    location.getChunk().getZ()
            );

            hologramsPerChunk.computeIfAbsent(chunkAddress, chunk -> new ArrayList<>()).add(new LocationMeta(
                    ID, coordinatesLocation.size(), i, location
            ));
            
            if (buildState) addedChunks.add(chunkAddress);
            
            i++;
        }

        if (buildState) {
            loadChunkTags(addedChunks);
        }
    }

    private static void loadChunkTags(Set<ChunkAddress> affectedChunks) {
        for (ChunkAddress chunk : affectedChunks) {
            clearChunk(chunk);

            hologramsBuilt.addAll(build(hologramsPerChunk.get(chunk)));
        }
    }

    public static void loadTags() {
        for (ChunkAddress chunk : hologramsPerChunk.keySet()) {
            hologramsBuilt.addAll(build(hologramsPerChunk.get(chunk)));
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        ChunkAddress chunk = new ChunkAddress(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());

        clearChunk(chunk);

        List<LocationMeta> locations = hologramsPerChunk.get(chunk);
        if (locations == null) return;

        hologramsBuilt.addAll(build(locations));
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent event) {
        ChunkAddress chunk = new ChunkAddress(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());

        clearChunk(chunk);
    }

    public static void cleanup() {
        List<World> worlds = Bukkit.getWorlds();

        hologramsBuilt.clear();

        for (ChunkAddress chunk : hologramsPerChunk.keySet()) {
            clearChunk(chunk);
        }

        // Extra redundant cleanup
        for (World world : worlds) {
            for (ArmorStand hg : world.getEntitiesByClass(ArmorStand.class)) {
                if (Utilities.hasID(hg.getPersistentDataContainer(), "hologram")) {
                    hg.remove();
                }
            }
        }
    }

    private static void clearChunk(ChunkAddress chunk) {
        for (Entity entity : chunk.world.getChunkAt(chunk.x, chunk.z).getEntities()) {
            if (!(entity instanceof ArmorStand)) continue;
            if (Utilities.hasID(entity.getPersistentDataContainer(), "hologram")) {
                entity.remove();
            }
        }
    }
}