package org.zeroxamr.parkourEX;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
                    || Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "resetParkour")
                    || Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "leaveParkour")) {
                event.setCancelled(true);
                return;
            }
        }

        if (cursor != null && cursor.getItemMeta() != null) {
            if (Utilities.hasID(cursor.getItemMeta().getPersistentDataContainer(), "checkpointNumber")
                    || Utilities.hasID(cursor.getItemMeta().getPersistentDataContainer(), "resetParkour")
                    || Utilities.hasID(cursor.getItemMeta().getPersistentDataContainer(), "leaveParkour")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropCheckpoint(PlayerDropItemEvent event) {
        if (Utilities.hasID(event.getItemDrop().getItemStack(), "checkpointNumber")
                || Utilities.hasID(event.getItemDrop().getItemStack(), "resetParkour")
                || Utilities.hasID(event.getItemDrop().getItemStack(), "leaveParkour")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Material type = item.getType();

        if ((type.equals(Material.RED_BED) || type.equals(Material.BARRIER))
                && (Utilities.hasID(item, "resetParkour") || Utilities.hasID(item, "leaveParkour"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().isRightClick() || event.getAction().isLeftClick()) {
            ItemStack item = event.getItem();

            if (item == null) return;
            if (!item.getType().equals(Material.ARROW)
                    && !item.getType().equals(Material.RED_BED)
                    && !item.getType().equals(Material.BARRIER)) return;

            if (Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "checkpointNumber")) {
                if (!player.isOnline()) return;
                if (player.hasMetadata("inParkour")
                        && player.hasMetadata("checkpointNumber")
                        && player.hasMetadata("checkpointLocation")) {
                    ParkourGame.playerStateCheckpoint(player);
                }
            }
            else if (Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "resetParkour")) {
                if (!player.isOnline()) return;
                if (player.hasMetadata("inParkour")
                        && player.hasMetadata("checkpointNumber")
                        && player.hasMetadata("checkpointLocation")) {
                    Location location = Main.getParkourGames().get(player.getMetadata("parkourID").getFirst().asInt()).getCheckpointMapWithYaw().firstEntry().getKey();
                    location.setX(location.getX() + 0.5);
                    location.setZ(location.getZ() + 0.5);

                    Vector direction = location.getDirection();
                    direction.setY(0).normalize().multiply(-1.5);
                    location.add(direction);

                    player.teleport(location);
                }
            }
            else if (Utilities.hasID(item.getItemMeta().getPersistentDataContainer(), "leaveParkour")) {
                if (!player.isOnline()) return;
                if (player.hasMetadata("inParkour")
                        && player.hasMetadata("checkpointNumber")
                        && player.hasMetadata("checkpointLocation")) {
                    Main.getParkourGames().get(player.getMetadata("parkourID").getFirst().asInt()).playerStateCancel(player);
                    player.sendMessage("§c§lParkour challenge cancelled!");
                }
            }
        }
    }
}