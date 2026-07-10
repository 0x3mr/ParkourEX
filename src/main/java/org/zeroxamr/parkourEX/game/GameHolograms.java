package org.zeroxamr.parkourEX.game;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jspecify.annotations.NonNull;
import org.zeroxamr.parkourEX.game.models.ChunkAddress;
import org.zeroxamr.parkourEX.game.models.LocationMeta;
import org.zeroxamr.parkourEX.util.Pdc;

import java.util.*;

public class GameHolograms {
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
        Pdc.set(hologram, id, idValue);

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
            Location location = locationMeta.location();
            String ID = locationMeta.ID();
            String State = locationMeta.index() == 0 ? "START" : locationMeta.index() == locationMeta.size() - 1 ? "END" : "CHECKPOINT";

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
                        "§b§l#" + locationMeta.index(), location.getWorld(),
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

    public static void register(int id, LinkedHashMap<Location, Integer> checkpoints) {
        for (Location loc : checkpoints.keySet()) {
            Location location = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());

            ChunkAddress chunkAddress = new ChunkAddress(
                    location.getWorld(), location.getChunk().getX(), location.getChunk().getZ()
            );

            hologramsPerChunk.computeIfAbsent(chunkAddress, chunk -> new ArrayList<>()).add(
                    new LocationMeta(
                            String.valueOf(id), checkpoints.size(), checkpoints.get(loc), location
            ));
        }
    }

    public static void resync() {
        for (ChunkAddress chunk : hologramsPerChunk.keySet()) {
            int expected = hologramsPerChunk.get(chunk).size() * 2;
            int actual = 0;

            for (Entity entity : chunk.world().getChunkAt(chunk.x(), chunk.z()).getEntities()) {
                if (!(entity instanceof ArmorStand)) continue;
                if (!Pdc.has(entity, "hologram")) continue;
                actual++;
            }

            if (expected != actual) {
                clearChunk(chunk);
                hologramsBuilt.removeIf(hg ->
                        hg.getWorld().equals(chunk.world()) &&
                        hg.getChunk().getX() == chunk.x() &&
                        hg.getChunk().getZ() == chunk.z()
                );
                hologramsBuilt.addAll(build(hologramsPerChunk.get(chunk)));
            }
        }
    }

    public static void loadTags() {
        for (ChunkAddress chunk : hologramsPerChunk.keySet()) {
            hologramsBuilt.addAll(build(hologramsPerChunk.get(chunk)));
        }
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
                if (Pdc.has(hg, "hologram")) {
                    hg.remove();
                }
            }
        }
    }

    public static void clearChunk(ChunkAddress chunk) {
        for (Entity entity : chunk.world().getChunkAt(chunk.x(), chunk.z()).getEntities()) {
            if (!(entity instanceof ArmorStand)) continue;
            if (Pdc.has(entity, "hologram")) {
                entity.remove();
            }
        }
    }

    public static List<LocationMeta> getChunk(ChunkAddress chunk) {
        return hologramsPerChunk.get(chunk);
    }

    public static void buildLocations(List<LocationMeta> locations) {
        hologramsBuilt.addAll(build(locations));
    }
}