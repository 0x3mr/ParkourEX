package org.zeroxamr.parkourEX.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.zeroxamr.parkourEX.Main;

public class Pdc {
    private static Main plugin;

    public static void initialize(Main plugin) {
        Pdc.plugin = plugin;
    }

    public static void set(ItemStack item, String key, String value) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        set(meta, key, value);
        item.setItemMeta(meta);
    }
    public static void set(ItemStack item, String key, int value) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        set(meta, key, value);
        item.setItemMeta(meta);
    }
    public static void set(ItemStack item, String key, boolean value) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        set(meta, key, value);
        item.setItemMeta(meta);
    }

    public static String getString(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return getString(meta, key);
    }
    public static Integer getInt(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return getInt(meta, key);
    }
    public static Boolean getBoolean(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return getBoolean(meta, key);
    }

    public static boolean has(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return has(meta, key);
    }

    private static NamespacedKey key(String key) { return new NamespacedKey(plugin, key); }

    public static void set(@NonNull PersistentDataHolder pdh, String key, String value) { pdh.getPersistentDataContainer().set(key(key), PersistentDataType.STRING, value); }
    public static void set(@NonNull PersistentDataHolder pdh, String key, int value) { pdh.getPersistentDataContainer().set(key(key), PersistentDataType.INTEGER, value); }
    public static void set(@NonNull PersistentDataHolder pdh, String key, boolean value) { pdh.getPersistentDataContainer().set(key(key), PersistentDataType.BOOLEAN, value); }

    public static String getString(@NonNull PersistentDataHolder pdh, String key) { return pdh.getPersistentDataContainer().get(key(key), PersistentDataType.STRING); }
    public static Integer getInt(@NonNull PersistentDataHolder pdh, String key) { return pdh.getPersistentDataContainer().get(key(key), PersistentDataType.INTEGER); }
    public static Boolean getBoolean(@NonNull PersistentDataHolder pdh, String key) { return pdh.getPersistentDataContainer().get(key(key), PersistentDataType.BOOLEAN); }

    public static Boolean has(@NonNull PersistentDataHolder pdh, String key) {
        PersistentDataContainer pdc = pdh.getPersistentDataContainer();
        NamespacedKey nsk = key(key);

        return pdc.has(nsk, PersistentDataType.STRING) ||
                pdc.has(nsk, PersistentDataType.INTEGER) ||
                pdc.has(nsk, PersistentDataType.BOOLEAN);
    }

    public static void remove(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        remove(meta, key);
        item.setItemMeta(meta);
    }

    public static void remove(@NonNull PersistentDataHolder pdh, String key) {
        pdh.getPersistentDataContainer().remove(key(key));
    }
}
