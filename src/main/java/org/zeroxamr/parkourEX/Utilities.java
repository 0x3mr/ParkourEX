package org.zeroxamr.parkourEX;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class Utilities {
    private static Main plugin = null;

    public static void initialize(Main plugin) {
        Utilities.plugin = plugin;
    }

    public static void resetPlayerInfo(Player e) {
        e.setMetadata("inParkour", new FixedMetadataValue(plugin, false));
        e.setMetadata("checkpointNumber", new FixedMetadataValue(plugin, -1));
        e.removeMetadata("parkourID", plugin);
        Services.removeLastCheckpoint(e);
        Services.removeResetParkour(e);
    }

    public static void resetPlayersInfo() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setMetadata("inParkour", new FixedMetadataValue(plugin, false));
            player.setMetadata("checkpointNumber", new FixedMetadataValue(plugin, -1));
            player.removeMetadata("parkourID", plugin);
            Services.removeLastCheckpoint(player);
            Services.removeResetParkour(player);
        }
    }

    public static String getDurationBetween(LocalTime duration1, LocalTime duration2) {
        Duration duration = Duration.between(duration1, duration2);

        long totalSeconds = duration.getSeconds();
//        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        long ms = duration.toMillis() % 1000;

        return String.format("%02d:%02d.%d", m, s, ms);
    }

    public static String serializeLocations(List<Location> locations) {
        StringBuilder fullString = new StringBuilder();
        int size = locations.size();
        for (int i = 0; i < size; i++) {
            Location location = locations.get(i);
            StringBuilder temp = new StringBuilder();
            temp.append(location.getWorld().getName()).append(",");
            temp.append(location.getX()).append(",");
            temp.append(location.getY()).append(",");
            temp.append(location.getZ()).append(",");
            temp.append(location.getYaw()).append(",");
            temp.append(location.getPitch()).append(",");
            if (!(i + 1 == size)) temp.append(";");
            fullString.append(temp);
        }
        return fullString.toString();
    }

    public static LinkedHashMap<Location, Integer> deserializeLocations(String locations) {
        LinkedHashMap<Location, Integer> parkourGame = new LinkedHashMap<>();
        String[] tempLocations = locations.split(";");
        int size = tempLocations.length;
        for (int i = 0; i < size; i++) {
            String[] tempLocation = tempLocations[i].split(",");
            if (tempLocation.length < 6) continue;
            Location location = new Location(
                    Bukkit.getWorld(tempLocation[0]),
                    Double.parseDouble(tempLocation[1]),
                    Double.parseDouble(tempLocation[2]),
                    Double.parseDouble(tempLocation[3]),
                    Float.parseFloat(tempLocation[4]),
                    Float.parseFloat(tempLocation[5])
            );
            parkourGame.put(location, i);
        }
        return parkourGame;
    }

    public static UUID generateRandomID() { return UUID.randomUUID(); }

    public static String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ() + "," +
                loc.getYaw() + "," +
                loc.getPitch();
    }

    public static Location deserializeLocation(String str) {
        String[] parts = str.split(",");
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
    }

    public static void attachID(ItemStack item, String id, String value) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey NSK = new NamespacedKey(plugin, id);
        meta.getPersistentDataContainer().set(NSK, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    public static void attachID(PersistentDataContainer PDC, String id, String value) {
        NamespacedKey NSK = new NamespacedKey(plugin, id);
        PDC.set(NSK, PersistentDataType.STRING, value);
    }

    public static Boolean hasID(ItemStack item, String id) {
        if (item == null || item.getType() == Material.AIR) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, id);
        return pdc.has(key, PersistentDataType.STRING);
    }

    public static Boolean hasID(PersistentDataContainer PDC, String id) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        return PDC.has(key, PersistentDataType.STRING);
    }

    public static String getAttachedID(ItemStack item, String id) {
        if (item == null) return "none";

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "none";

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, id);

        if (pdc.has(key, PersistentDataType.STRING)) {
            return pdc.get(key, PersistentDataType.STRING);
        }

        return "none";
    }

    public static Boolean doCloneExist(LinkedHashMap<Location, Integer> newLocations) {
        for (Map.Entry<UUID, ParkourGame> pg : Main.getParkourGames().entrySet()) {
            LinkedHashMap<Location, Integer> existingLocations = pg.getValue().getCheckpointMap();
            for (Location newLoc : newLocations.keySet()) {
                for (Location existingLoc : existingLocations.keySet()) {
                    if (newLoc.getBlockX() == existingLoc.getBlockX() &&
                        newLoc.getBlockY() == existingLoc.getBlockY() &&
                        newLoc.getBlockZ() == existingLoc.getBlockZ()) {
                        plugin.getLogger().info("Existing parkour found! Failed to save parkour locations.");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
