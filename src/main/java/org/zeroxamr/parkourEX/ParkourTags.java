package org.zeroxamr.parkourEX;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParkourTags {
    public static void build(List<Location> coordinatesLocation, String ID) {
        World world = coordinatesLocation.getFirst().getWorld();
        int size = coordinatesLocation.size();
        for (int i = 0; i < size; i++) {
            Location loc = coordinatesLocation.get(i).clone();
            loc.setX(loc.getX() + 0.5);
            loc.setZ(loc.getZ() + 0.5);
            ArmorStand hg = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            Utilities.attachID(hg.getPersistentDataContainer(), "hologram", i + "." + ID);
            hg.setVisible(false);
            hg.setCustomNameVisible(true);
            hg.setGravity(false);

            if (i == 0) {
                hg.setCustomName("" + ChatColor.GREEN + ChatColor.BOLD + "Start");
            } else if (i == size - 1) {
                hg.setCustomName("" + ChatColor.RED + ChatColor.BOLD + "End");
            } else {
                hg.setCustomName("" + ChatColor.YELLOW + ChatColor.BOLD + "Checkpoint" + ChatColor.AQUA + ChatColor.BOLD + " #" + i);
            }
        }
    }

    public static void cleanup() {
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds) {
            for (ArmorStand hg : world.getEntitiesByClass(ArmorStand.class)) {
                if (Utilities.hasID(hg.getPersistentDataContainer(), "hologram")) {
                    hg.remove();
                }
            }
        }
    }
}
