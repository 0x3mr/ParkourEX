package org.zeroxamr.parkourEX.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.config.Config;
import org.zeroxamr.parkourEX.game.GameInstance;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.util.Pdc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameItems implements Listener {
    private static final HashMap<String, ItemStack> PARKOUR_ITEMS = new HashMap<>();

public static void createItems() {
    Material material = Material.matchMaterial(Config.RESET_ITEM.stringValue());
    if (material == null) {
        Main.getPlugin().getLogger().info("Failed to read RESET_ITEM material, falling back to RED_BED");
        material = Material.RED_BED;
    }
    ItemStack item = new ItemStack(material);
    ItemMeta arr = item.getItemMeta();
    arr.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Reset");
    item.setItemMeta(arr);
    Pdc.set(item, "parkourItem", "reset");

    PARKOUR_ITEMS.put("RESET_ITEM", item);

    material = Material.matchMaterial(Config.CANCEL_ITEM.stringValue());
    if (material == null) {
        Main.getPlugin().getLogger().info("Failed to read CANCEL_ITEM material, falling back to OAK_DOOR");
        material = Material.OAK_DOOR;
    }
    item = new ItemStack(material);
    arr = item.getItemMeta();
    arr.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "Cancel");
    item.setItemMeta(arr);
    Pdc.set(item, "parkourItem", "cancel");

    PARKOUR_ITEMS.put("CANCEL_ITEM", item);

    material = Material.matchMaterial(Config.CHECKPOINT_ITEM.stringValue());
    if (material == null) {
        Main.getPlugin().getLogger().info("Failed to read CHECKPOINT_ITEM material, falling back to ARROW");
        material = Material.ARROW;
    }
    item = new ItemStack(material);
    arr = item.getItemMeta();
    arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Teleport to Last Checkpoint");
    item.setItemMeta(arr);
    Pdc.set(item, "parkourItem", "checkpoint");

    PARKOUR_ITEMS.put("CHECKPOINT_ITEM", item);
}

    public static ItemStack reset() {
        return PARKOUR_ITEMS.get("RESET_ITEM");
    }

    public static ItemStack cancel() {
        return PARKOUR_ITEMS.get("CANCEL_ITEM");
    }

    public static ItemStack checkpoint() {
        return PARKOUR_ITEMS.get("CHECKPOINT_ITEM");
    }

    @EventHandler
    public void onPlayerInvClickParkourItem(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))
                && (Pdc.has(item, "parkourItem")
                || Pdc.has(cursor, "parkourItem"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropParkourItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))
                && Pdc.has(item, "parkourItem")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractParkourItem(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Player player = event.getPlayer();

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))
                && Pdc.has(item, "parkourItem")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClickParkourItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnline()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (!PARKOUR_ITEMS.containsValue(item)) return;

        if (Boolean.FALSE.equals(Pdc.getBoolean(player, "inParkour"))) return;

        Integer gameID = Pdc.getInt(player, "parkourID");

        if (!GameRegistry.hasGame(gameID)) {
            player.sendMessage("§cYou're playing an invalid parkour session!");
            return;
        }

        if ("checkpoint".equals(Pdc.getString(item, "parkourItem"))) {
            GameInstance.playerStateCheckpoint(player);
        }
        else if ("reset".equals(Pdc.getString(item, "parkourItem"))) {
            Location location = GameRegistry.getParkourGame(gameID).getCheckpointMapWithYaw().firstEntry().getKey();
            location.setX(location.getX() + 0.5);
            location.setZ(location.getZ() + 0.5);

            Vector direction = location.getDirection();
            direction.setY(0).normalize().multiply(-1.5);
            location.add(direction);

            player.teleport(location);
        }
        else if ("cancel".equals(Pdc.getString(item, "parkourItem"))) {
            GameRegistry.getParkourGame(gameID).playerStateCancel(player);
            player.sendMessage("§c§lParkour challenge cancelled!");
            GameRegistry.executeExitCommands(gameID, player);
        }
    }
}