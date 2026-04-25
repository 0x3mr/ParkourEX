package org.zeroxamr.parkourEX;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    private static Main plugin = null;

    public static void initialize(Main plugin) {
        Commands.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("psetup")) {
            if (!(sender instanceof Player)) {
                return false;
            }

            Services.setupParkour((Player) sender);
        }
        return true;
    }
}
