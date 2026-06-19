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
        ItemStack item = event.getItemInHand();
        String status = Utilities.getAttachedID(item, "cp-state");
        if (status.equals("none")) return;

        Location loc = event.getBlockPlaced().getLocation();
        event.getPlayer().getWorld().getBlockAt(loc).setType(Material.AIR);

        String id = Utilities.getAttachedID(item, "cp-id");
        UUID uuid = UUID.fromString(id);

        switch (status) {
            case "start":
                event.getPlayer().getInventory().clear(event.getPlayer().getInventory().getHeldItemSlot());

                loc.setPitch(0);
                loc.setYaw(event.getPlayer().getYaw());

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

                event.getPlayer().getInventory().setItem(4, newItem);
                event.getPlayer().getInventory().setItem(5, newItem2);

                event.getPlayer().sendMessage("" + ChatColor.GOLD + "Select the location of your next checkpoint.");
                break;
            case "checkpoint":
                loc.setPitch(0);
                loc.setYaw(event.getPlayer().getYaw());

                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                event.getPlayer().sendMessage("" + ChatColor.GREEN + "Checkpoint #" + (createdGames.get(uuid).size() - 1) + " saved!");
                event.getPlayer().sendMessage("" + ChatColor.GOLD + "Select the location of your next checkpoint.");
                break;
            case "finish":
                loc.setPitch(0);
                loc.setYaw(event.getPlayer().getYaw());

                createdGames.get(uuid).put(loc, createdGames.get(uuid).size());

                for (ItemStack tempItem : event.getPlayer().getInventory()) {
                    if (tempItem != null && Utilities.hasID(tempItem, "cp-state")) {
                        event.getPlayer().getInventory().clear(event.getPlayer().getInventory().first(tempItem));
                    }
                }

                if (Services.registerParkour(createdGames.get(uuid), uuid)) {
                    plugin.getLogger().info("Successfully registered parkour game: " + uuid.toString());

                    event.getPlayer().sendMessage("" + ChatColor.GREEN + "New parkour created!");
                } else {
                    event.getPlayer().sendMessage("" + ChatColor.RED + "The parkour was not saved! Check console for more details.");
                }
        }
    }

    public static Boolean registerParkour(LinkedHashMap<Location, Integer> locations, UUID uuid) {
        if (Utilities.doCloneExist(locations)) return false;

        ParkourGame parkourGame = new ParkourGame(plugin, uuid, locations);
        getServer().getPluginManager().registerEvents(parkourGame, plugin);
        Main.getParkourGames().put(uuid, parkourGame);
        Main.getDBM().saveGame(parkourGame);

        return true;
    }
}
