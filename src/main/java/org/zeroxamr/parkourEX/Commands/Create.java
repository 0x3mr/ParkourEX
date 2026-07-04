package org.zeroxamr.parkourEX.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.Services;

public class Create implements Base {
    @Override
    public String getName() {
        return "Create";
    }

    @Override
    public String getInfo() {
        return "Creates a new parkour course";
    }

    @Override
    public String getUsage() {
        return "/parkourex create";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("" + ChatColor.RED + "Command restricted to in-game players only.");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage("" + ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("" + ChatColor.RED + "Incorrect command usage.");
            sender.sendMessage("" + ChatColor.RED + "Use " + ChatColor.YELLOW + getUsage() + ChatColor.RED + ".");
            return true;
        }

        Services.setupParkour((Player) sender);

        return true;
    }
}
