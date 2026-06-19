package org.zeroxamr.parkourEX;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.N;

import java.util.UUID;

public class ParkourItems implements Listener {
    private static Main plugin = null;

    public static void initialize(Main plugin) {
        ParkourItems.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInventoryInteract(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (item != null && item.getItemMeta() != null) {
            if (Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "checkpointNumber")
                || Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "resetParkour")) {
                event.setCancelled(true);
                return;
            }
        }

        if (cursor != null && cursor.getItemMeta() != null) {
            if (Utilities.hasID(cursor.getItemMeta().getPersistentDataContainer(), "checkpointNumber")
                || Utilities.hasID(cursor.getItemMeta().getPersistentDataContainer(), "resetParkour")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropCheckpoint(PlayerDropItemEvent event) {
        if (Utilities.hasID(event.getItemDrop().getItemStack(), "checkpointNumber")
            || Utilities.hasID(event.getItemDrop().getItemStack(), "resetParkour")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.getType().equals(Material.RED_BED)) {
            if (Utilities.hasID(item, "resetParkour")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().isRightClick() || event.getAction().isLeftClick()) {
            ItemStack item = event.getItem();

            if (item == null) return;
            if (!item.getType().equals(Material.ARROW)
                && !item.getType().equals(Material.RED_BED)) return;

            if (Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "checkpointNumber")) {
                if (!player.isOnline()) return;
                if (player.hasMetadata("inParkour")
                        && player.hasMetadata("checkpointNumber")
                        && player.hasMetadata("checkpointLocation")) {
                    Location location = Utilities.deserializeLocation(player.getMetadata("checkpointLocation").getFirst().asString());
                    location.setX(location.getX() + 0.5);
                    location.setZ(location.getZ() + 0.5);
                    player.teleport(location);
                }
            }
            else if (Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "resetParkour")) {
                if (!player.isOnline()) return;
                if (player.hasMetadata("inParkour")
                        && player.hasMetadata("checkpointNumber")
                        && player.hasMetadata("checkpointLocation")) {
                    Main.getParkourGames().get(UUID.fromString(player.getMetadata("parkourID").getFirst().asString())).setPlayerState(player);

                    Location location = Main.getParkourGames().get(UUID.fromString(player.getMetadata("parkourID").getFirst().asString())).getCheckpointMapWithYaw().firstEntry().getKey();
                    location.setX(location.getX() + 0.5);
                    location.setZ(location.getZ() + 0.5);

                    Vector direction = location.getDirection();
                    direction.setY(0).normalize().multiply(-1.5);
                    location.add(direction);

                    player.teleport(location);
                }
            }
        }
    }
}