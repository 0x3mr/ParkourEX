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
import org.zeroxamr.parkourEX.util.Shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameItems implements Listener {
    private static final HashMap<String, ItemStack> PARKOUR_ITEMS = new HashMap<>();

    public static void createItems() {
        PARKOUR_ITEMS.put("RESET_ITEM", Shared.createItem(
                "§c§lReset",
                "parkourItem",
                "RESET_ITEM",
                Config.RESET_ITEM.stringValue()
        ));

        PARKOUR_ITEMS.put("CANCEL_ITEM", Shared.createItem(
                "§e§lCancel",
                "parkourItem",
                "CANCEL_ITEM",
                Config.CANCEL_ITEM.stringValue()
        ));

        PARKOUR_ITEMS.put("CHECKPOINT_ITEM", Shared.createItem(
                "§a§lTeleport to Last Checkpoint",
                "parkourItem",
                "CHECKPOINT_ITEM",
                Config.CHECKPOINT_ITEM.stringValue()
        ));
    }

    public static ItemStack getItem(String itemName) { return PARKOUR_ITEMS.get(itemName); }

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

        if ("CHECKPOINT_ITEM".equals(Pdc.getString(item, "parkourItem"))) {
            GameInstance.playerStateCheckpoint(player);
        }
        else if ("RESET_ITEM".equals(Pdc.getString(item, "parkourItem"))) {
            Location location = GameRegistry.getParkourGame(gameID).getCheckpointMapWithYaw().firstEntry().getKey();
            location.setX(location.getX() + 0.5);
            location.setZ(location.getZ() + 0.5);

            Vector direction = location.getDirection();
            direction.setY(0).normalize().multiply(-1.5);
            location.add(direction);

            player.teleport(location);
        }
        else if ("CANCEL_ITEM".equals(Pdc.getString(item, "parkourItem"))) {
            GameRegistry.getParkourGame(gameID).playerStateCancel(player);
            player.sendMessage("§c§lParkour challenge cancelled!");
            GameRegistry.executeExitCommands(gameID, player);
        }
    }
}