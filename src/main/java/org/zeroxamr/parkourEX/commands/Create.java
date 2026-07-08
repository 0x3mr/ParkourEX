package org.zeroxamr.parkourEX.commands;

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
        return "/pkx create";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("" + ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cIncorrect command usage.");
            sender.sendMessage("§cUse §e" + getUsage() + "§c.");
            return true;
        }

        Services.giveCreateParkour((Player) sender);

        return true;
    }
}
