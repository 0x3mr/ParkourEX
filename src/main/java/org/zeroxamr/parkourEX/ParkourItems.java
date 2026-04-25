package org.zeroxamr.parkourEX;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ParkourItems implements Listener {
    private static Main plugin = null;

    public static void initialize(Main plugin) {
        ParkourItems.plugin = plugin;
    }

//    @EventHandler
//    public void onPlayerMoveCheckpoint(InventoryPickupItemEvent event) {
//        ItemStack item = event.getItem().getItemStack();
//        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
//        if (pdc.has(Services.key, PersistentDataType.STRING)) {
//            event.setCancelled(true);
//        }
//    }

    @EventHandler
    public void onPlayerInventoryInteract(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (item != null && item.getItemMeta() != null) {
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(Services.key, PersistentDataType.STRING)) {
                event.setCancelled(true);
//                event.getWhoClicked().closeInventory();
                return;
            }
        }

        if (cursor != null && cursor.getItemMeta() != null) {
            PersistentDataContainer pdc2 = cursor.getItemMeta().getPersistentDataContainer();
            if (pdc2.has(Services.key, PersistentDataType.STRING)) {
                event.setCancelled(true);
//                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onPlayerDropCheckpoint(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(Services.key, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().isRightClick() || event.getAction().isLeftClick()) {
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!item.getType().equals(Material.ARROW)) return;
            PersistentDataContainer PDC = item.getItemMeta().getPersistentDataContainer();
            //        player.sendMessage("booboo");
            if (PDC.has(Services.key, PersistentDataType.STRING)) {
                Services.sendToLastCheckpoint(player);
            }
        }
    }
}
