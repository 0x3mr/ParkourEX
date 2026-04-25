package org.zeroxamr.parkourEX;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class Services {
    private static Main plugin = null;
    static NamespacedKey key = null;

    public static void initialize(Main plugin) {
        Services.plugin = plugin;
        Services.key = new NamespacedKey(plugin, "checkpoint");
    }

    public static void giveLastCheckpoint(Player player) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta arr = item.getItemMeta();

        arr.setDisplayName("" + ChatColor.GREEN + "Teleport to Last Checkpoint");
        arr.getPersistentDataContainer().set(Services.key, PersistentDataType.STRING, "checkpoint");

        item.setItemMeta(arr);

//        player.getInventory().addItem(item);
        player.getInventory().setItem(plugin.getConfig().getInt("checkpointSlot"), item);
    }

    public static void removeLastCheckpoint(Player player) {
        if (!player.getInventory().contains(Material.ARROW)) return;

        ItemStack item = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
        if (item == null) return;

        ItemMeta arr = item.getItemMeta();
        if (arr == null) return;

        PersistentDataContainer pdc = arr.getPersistentDataContainer();
        if (Objects.equals(pdc.get(Services.key, PersistentDataType.STRING), "checkpoint")) {
            player.getInventory().remove(item);
        }
    }

    public static void sendToLastCheckpoint(Player player) {
        if (!player.isOnline()) return;
        if (player.hasMetadata("inParkour") && player.hasMetadata("checkpoint") && player.hasMetadata("lastCheckpoint")) {
            Location location = Utilities.deserializeLocation(player.getMetadata("lastCheckpoint").getFirst().asString());
            location.setX(location.getX() + 0.5);
            location.setZ(location.getZ() + 0.5);
            player.teleport(location);
        }
    }

    public static void setupParkour(Player player) {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta arr = item.getItemMeta();
        arr.addEnchant(Enchantment.UNBREAKING, 1, true);
        arr.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        arr.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Set a checkpoint");
//        NamespacedKey NSK = new NamespacedKey(plugin, "setCheckpoint");
//        arr.getPersistentDataContainer().set(NSK, PersistentDataType.STRING, "setCheckpoint");

        item.setItemMeta(arr);

        player.getInventory().setItem(3, item);
    }
}
