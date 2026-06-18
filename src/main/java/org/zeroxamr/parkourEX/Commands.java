package org.zeroxamr.parkourEX;

import org.bukkit.ChatColor;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command restricted to in-game players only.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage("" + ChatColor.RED + "Use /parkourex help for a list of commands.");
            return true;
        }

        switch(args[0]) {
            case "create":
                if (!player.isOp()) {
                    player.sendMessage("" + ChatColor.RED + "No permission.");
                    break;
                }
                if (args.length != 1) {
                    player.sendMessage("" + ChatColor.RED + "Create parkour usage:\n/parkourex create");
                    break;
                }
                Services.setupParkour(player);
                break;
            case "help":
                player.sendMessage("\n");
                player.sendMessage(" " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ParkourEx" + ChatColor.RESET + ChatColor.GRAY + " - List of commands:");
                player.sendMessage("   " + ChatColor.GRAY + "- " + ChatColor.WHITE + "/parkourex create");
                player.sendMessage("   " + ChatColor.GRAY + "- " + ChatColor.WHITE + "/parkourex help");
                break;
            default:
                player.sendMessage("" + ChatColor.RED + "Use /parkourex help for a list of commands.");
        }

        return true;
    }
}
