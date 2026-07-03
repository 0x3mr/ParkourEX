package org.zeroxamr.parkourEX;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class Services implements Listener {
    private static Main plugin = null;
    static NamespacedKey key = null;
    private static final HashMap<UUID, LinkedHashMap<Location, Integer>> createdGames = new HashMap<UUID, LinkedHashMap<Location, Integer>>();

    public static void initialize(Main plugin) {
        Services.plugin = plugin;
        Services.key = new NamespacedKey(plugin, "checkpointNumber");
    }

    public static void addResetParkour(Player player) {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Reset");
        item.setItemMeta(arr);

        Utilities.attachID(item, "resetParkour", "resetParkour");

        player.getInventory().setItem(plugin.getConfig().getInt("resetSlot"), item);
    }

    public static void removeResetParkour(Player player) {
        if (!player.getInventory().contains(Material.RED_BED)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.RED_BED));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        PersistentDataContainer pdc = arr.getPersistentDataContainer();
        if (Objects.equals(pdc.get(new NamespacedKey(plugin, "resetParkour"), PersistentDataType.STRING), "resetParkour")) {
            player.getInventory().remove(item);
        }
    }

    public static void addLeaveParkour(Player player) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "Exit");
        item.setItemMeta(arr);

        Utilities.attachID(item, "leaveParkour", "leaveParkour");

        player.getInventory().setItem(plugin.getConfig().getInt("leaveSlot"), item);
    }

    public static void removeLeaveParkour(Player player) {
        if (!player.getInventory().contains(Material.BARRIER)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.BARRIER));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        PersistentDataContainer pdc = arr.getPersistentDataContainer();
        if (Objects.equals(pdc.get(new NamespacedKey(plugin, "leaveParkour"), PersistentDataType.STRING), "leaveParkour")) {
            player.getInventory().remove(item);
        }
    }

    public static void addLastCheckpoint(Player player) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Teleport to Last Checkpoint");
        item.setItemMeta(arr);

        Utilities.attachID(item, "checkpointNumber", "checkpointNumber");

        player.getInventory().setItem(plugin.getConfig().getInt("checkpointSlot"), item);
    }

    public static void removeLastCheckpoint(Player player) {
        if (!player.getInventory().contains(Material.ARROW)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        PersistentDataContainer pdc = arr.getPersistentDataContainer();
        if (Objects.equals(pdc.get(new NamespacedKey(plugin, "checkpointNumber"), PersistentDataType.STRING), "checkpointNumber")) {
            player.getInventory().remove(item);
        }
    }

    public static void setupParkour(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && Utilities.hasID(item, "cp-state")) {
                player.sendMessage("" + ChatColor.RED + "You're already setting up a parkour!");
                return;
            }
        }

        String uuid = Utilities.generateRandomID().toString();
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta arr = item.getItemMeta();

        arr.addEnchant(Enchantment.UNBREAKING, 1, true);
        arr.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Set [Start]");

        item.setItemMeta(arr);

        Utilities.attachID(item, "cp-id", uuid);
        Utilities.attachID(item, "cp-state", "start");

        player.getInventory().setItem(4, item);
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + "Place the [Start] block to set the parkour's first checkpoint");
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + "TIP: The direction you face while placing a checkpoint will be used as the respawn orientation for that checkpoint");
    }

    @EventHandler
    public void createParkour(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        String status = Utilities.getAttachedID(item, "cp-state");
        if (status.equals("none")) return;

        Location loc = event.getBlockPlaced().getLocation();
        event.setCancelled(true);

        String id = Utilities.getAttachedID(item, "cp-id");
        UUID uuid = UUID.fromString(id);

        boolean isDuplicate = createdGames.containsKey(uuid) &&
                createdGames.get(uuid).keySet().stream().anyMatch(location ->
                location.getWorld().getName().equals(loc.getWorld().getName()) &&
                location.getBlockX() == loc.getBlockX() &&
                location.getBlockY() == loc.getBlockY() &&
                location.getBlockZ() == loc.getBlockZ()
        );

        if (isDuplicate) {
            player.sendMessage("" + ChatColor.RED + "You have already set this location!");
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
                arr.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + "Set [Checkpoint]");
                newItem.setItemMeta(arr);

                Utilities.attachID(newItem, "cp-id", id);
                Utilities.attachID(newItem, "cp-state", "checkpoint");

                ItemStack newItem2 = new ItemStack(Material.RED_WOOL);
                ItemMeta arr2 = newItem2.getItemMeta();
                arr2.addEnchant(Enchantment.UNBREAKING, 1, true);
                arr2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                arr2.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Set [Finish]");
                newItem2.setItemMeta(arr2);

                Utilities.attachID(newItem2, "cp-id", id);
                Utilities.attachID(newItem2, "cp-state", "finish");

                player.getInventory().setItem(4, newItem);
                player.getInventory().setItem(5, newItem2);

                player.sendMessage("" + ChatColor.GOLD + "Select the location of your next checkpoint.");

                break;
            case "checkpoint":
                loc.setPitch(0);
                loc.setYaw(player.getYaw());

                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                player.sendMessage("" + ChatColor.GREEN + "Checkpoint #" + (createdGames.get(uuid).size() - 1) + " saved!");
                player.sendMessage("" + ChatColor.GOLD + "Select the location of your next checkpoint.");

                break;
            case "finish":
                loc.setPitch(0);
                loc.setYaw(player.getYaw());

                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                for (ItemStack tempItem : player.getInventory()) {
                    if (tempItem != null && Utilities.hasID(tempItem, "cp-state")) {
                        player.getInventory().clear(player.getInventory().first(tempItem));
                    }
                }

                if (Utilities.doCloneExist(createdGames.get(uuid))) {
                    player.sendMessage("" + ChatColor.RED + "Parkour did not save.\nThis parkour intervenes with another existing parkour!");
                    return;
                }

                if (Main.getDBM().saveGame(createdGames.get(uuid), player.getName())) {
                    player.sendMessage("" + ChatColor.GREEN + "New parkour created!");
                }
                else {
                    player.sendMessage("" + ChatColor.RED + "Failed to save new parkour. Check console for more details.");
                }
        }
    }
}
