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
    private static final HashMap<UUID, LinkedHashMap<Location, Integer>> createdGames = new HashMap<>();

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

    @EventHandler
    public void onParkourSetup(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        String status = Pdc.getString(item, "cp-state");
        if (status == null) return;

        Location loc = event.getBlockPlaced().getLocation();
        event.setCancelled(true);

        String id = Pdc.getString(item, "cp-id");
        UUID uuid = UUID.fromString(id);

        boolean isDuplicate = createdGames.containsKey(uuid) &&
                createdGames.get(uuid).keySet().stream().anyMatch(location ->
                location.getWorld().getName().equals(loc.getWorld().getName()) &&
                location.getBlockX() == loc.getBlockX() &&
                location.getBlockY() == loc.getBlockY() &&
                location.getBlockZ() == loc.getBlockZ()
        );

        if (isDuplicate) {
            player.sendMessage("§cYou have already set this location!");
            return;
        }

        switch (status) {
            case "start":
                player.getInventory().clear(player.getInventory().getHeldItemSlot());

                loc.setPitch(0);
                loc.setYaw(player.getYaw());

                createdGames.put(uuid, new LinkedHashMap<>());
                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                ItemStack newItem = new ItemStack(Material.ORANGE_WOOL);
                ItemMeta arr = newItem.getItemMeta();

                arr.addEnchant(Enchantment.UNBREAKING, 1, true);
                arr.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                arr.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + "[Checkpoint]");

                newItem.setItemMeta(arr);

                Pdc.set(newItem, "cp-id", id);
                Pdc.set(newItem, "cp-state", "checkpoint");

                ItemStack newItem2 = new ItemStack(Material.RED_WOOL);
                ItemMeta arr2 = newItem2.getItemMeta();

                arr2.addEnchant(Enchantment.UNBREAKING, 1, true);
                arr2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                arr2.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "[End]");

                newItem2.setItemMeta(arr2);

                Pdc.set(newItem2, "cp-id", id);
                Pdc.set(newItem2, "cp-state", "end");

                player.getInventory().setItem(4, newItem);
                player.getInventory().setItem(5, newItem2);

                player.sendMessage("§6Select the location of your next checkpoint.");

                break;
            case "checkpoint":
                loc.setPitch(0);
                loc.setYaw(player.getYaw());

                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                player.sendMessage("§aCheckpoint #" + (createdGames.get(uuid).size() - 1) + " saved!");
                player.sendMessage("§6Select the location of your next checkpoint.");

                break;
            case "end":
                loc.setPitch(0);
                loc.setYaw(player.getYaw());

                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                for (ItemStack tempItem : player.getInventory()) {
                    if (tempItem != null && Pdc.has(tempItem, "cp-state")) {
                        player.getInventory().clear(player.getInventory().first(tempItem));
                    }
                }

                if (Shared.doCloneExist(createdGames.get(uuid))) {
                    player.sendMessage("§cParkour did not save.\nThis parkour intervenes with another existing parkour!");
                    return;
                }

                player.sendMessage("§7Saving parkour...");

                if (Main.getDBM().saveGame(createdGames.get(uuid), player.getName())) {
                    player.sendMessage("§aNew parkour created!");
                }
                else {
                    player.sendMessage("§cFailed to save new parkour. Check console for more details.");
                }
        }
    }
}
