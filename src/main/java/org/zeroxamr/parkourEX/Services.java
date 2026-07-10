package org.zeroxamr.parkourEX;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zeroxamr.parkourEX.util.Pdc;
import org.zeroxamr.parkourEX.util.Shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Services implements Listener {
    private static Main plugin = null;

    public static void initialize(Main plugin) {
        Services.plugin = plugin;
    }

    public static void addResetParkour(Player player) {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Reset");
        item.setItemMeta(arr);

        Pdc.set(item, "parkourItem", "reset");

        player.getInventory().setItem(plugin.getConfig().getInt("resetSlot"), item);
    }

    public static void removeResetParkour(Player player) {
        if (!player.getInventory().contains(Material.RED_BED)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.RED_BED));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        if (Pdc.has(arr, "parkourItem")) {
            player.getInventory().remove(item);
        }
    }

    public static void addLeaveParkour(Player player) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "Cancel");
        item.setItemMeta(arr);

        Pdc.set(item, "parkourItem", "cancel");

        player.getInventory().setItem(plugin.getConfig().getInt("leaveSlot"), item);
    }

    public static void removeLeaveParkour(Player player) {
        if (!player.getInventory().contains(Material.BARRIER)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.BARRIER));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        if (Pdc.has(arr, "parkourItem")) {
            player.getInventory().remove(item);
        }
    }

    public static void addLastCheckpoint(Player player) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Teleport to Last Checkpoint");
        item.setItemMeta(arr);

        Pdc.set(item, "parkourItem", "checkpoint");

        player.getInventory().setItem(plugin.getConfig().getInt("checkpointSlot"), item);
    }

    public static void removeLastCheckpoint(Player player) {
        if (!player.getInventory().contains(Material.ARROW)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        if (Pdc.has(arr, "parkourItem")) {
            player.getInventory().remove(item);
        }
    }

    public static void giveCreateParkour(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && Pdc.has(item, "cp-state")) {
                player.sendMessage("§cYou're already setting up a parkour!");
                return;
            }
        }

        String uuid = Shared.generateRandomID().toString();
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta arr = item.getItemMeta();

        arr.addEnchant(Enchantment.UNBREAKING, 1, true);
        arr.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "[Start]");

        item.setItemMeta(arr);

        Pdc.set(item, "cp-id", uuid);
        Pdc.set(item, "cp-state", "start");

        player.getInventory().setItem(4, item);
        player.sendMessage("§dPlace the [Start] block to set the parkour's first checkpoint");
        player.sendMessage("§dTIP: The direction you face while placing a checkpoint will be used as the respawn orientation for that checkpoint");
    }
}
