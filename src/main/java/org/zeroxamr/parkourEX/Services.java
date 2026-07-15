package org.zeroxamr.parkourEX;

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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.zeroxamr.parkourEX.listeners.GameItems;
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
        ItemStack item = new ItemStack(GameItems.reset());
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Reset");
        item.setItemMeta(arr);

        Pdc.set(item, "parkourItem", "reset");

        player.getInventory().setItem(plugin.getConfig().getInt("resetSlot"), item);
    }

    public static void removeResetParkour(Player player) {
        if (!player.getInventory().contains(GameItems.reset())) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(GameItems.reset()));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        if (Pdc.has(arr, "parkourItem")) {
            player.getInventory().remove(item);
        }
    }

    public static void addLeaveParkour(Player player) {
        ItemStack item = new ItemStack(GameItems.cancel());
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "Cancel");
        item.setItemMeta(arr);

        Pdc.set(item, "parkourItem", "cancel");

        player.getInventory().setItem(plugin.getConfig().getInt("cancelSlot"), item);
    }

    public static void removeLeaveParkour(Player player) {
        if (!player.getInventory().contains(GameItems.cancel())) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(GameItems.cancel()));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        if (Pdc.has(arr, "parkourItem")) {
            player.getInventory().remove(item);
        }
    }

    public static void addLastCheckpoint(Player player) {
        ItemStack item = new ItemStack(GameItems.checkpoint());
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Teleport to Last Checkpoint");
        item.setItemMeta(arr);

        Pdc.set(item, "parkourItem", "checkpoint");

        player.getInventory().setItem(plugin.getConfig().getInt("checkpointSlot"), item);
    }

    public static void removeLastCheckpoint(Player player) {
        if (!player.getInventory().contains(GameItems.checkpoint())) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(GameItems.checkpoint()));
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

        Pdc.set(player, "cp-id", uuid);
        Pdc.set(item, "cp-id", uuid);
        Pdc.set(item, "cp-state", "start");

        player.getInventory().setItem(4, item);
        player.sendMessage("§dPlace the [Start] block to set the parkour's first checkpoint");
        player.sendMessage("§dTIP: The direction you face while placing a checkpoint will be used as the respawn orientation for that checkpoint");
    }

    public static void disableCollision(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("parkourCollisions");
        if (team == null) {
            team = scoreboard.registerNewTeam("parkourCollisions");
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        team.addEntry(player.getName());
    }

    public static void resetCollisionToDefault(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("parkourCollisions");
        if (team == null) {
            return;
        }
        team.removeEntry(player.getName());
    }
}
