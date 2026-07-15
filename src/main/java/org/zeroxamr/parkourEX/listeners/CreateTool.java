package org.zeroxamr.parkourEX.listeners;

import org.bukkit.Bukkit;
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
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.util.Pdc;
import org.zeroxamr.parkourEX.util.Shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class CreateTool implements Listener {
    private static Main plugin;

    private static final HashMap<UUID, LinkedHashMap<Location, Integer>> createdGames = new HashMap<>();

    public static void initialize(Main plugin) {
        CreateTool.plugin = plugin;
    }

    public static void removeGame(UUID uuid) {
        createdGames.remove(uuid);
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
        if (id == null) return;

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

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    boolean result = Main.getDBM().saveGame(createdGames.get(uuid), player.getName());

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (result) {
                            player.sendMessage("§aNew parkour created!");
                        }
                        else {
                            player.sendMessage("§cFailed to save new parkour. Check console for more details.");
                        }

                        removeGame(uuid);
                    });
                });

        }
    }
}
