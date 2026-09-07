package org.zeroxamr.parkourEX.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.Services;
import org.zeroxamr.parkourEX.listeners.CreateTool;
import org.zeroxamr.parkourEX.util.Pdc;

import java.util.UUID;

public class List implements Base {
    @Override
    public String getName() {
        return "List";
    }

    @Override
    public String getInfo() {
        return "Lists all created parkour runs";
    }

    @Override
    public String getUsage() {
        return "/parkour list";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!sender.isOp()) {
            player.sendMessage("" + ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cIncorrect command usage.");
            player.sendMessage("§cUse §e" + getUsage() + "§c.");
            return true;
        }

        Services.sendParkourList(player);

        return true;
    }
}
