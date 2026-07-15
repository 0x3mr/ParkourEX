package org.zeroxamr.parkourEX.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.game.GameInstance;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.util.Pdc;

public class GameItems implements Listener {
    private static Main plugin;

    public static void initialize(Main plugin) {
        GameItems.plugin = plugin;
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

        // TODO: let this dynamically checked, and be configurable
        if (!item.getType().equals(Material.ARROW)
                && !item.getType().equals(Material.RED_BED)
                && !item.getType().equals(Material.BARRIER)) {
            return;
        }

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
        }
    }
}