package org.zeroxamr.parkourEX.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.Services;
import org.zeroxamr.parkourEX.listeners.CreateTool;
import org.zeroxamr.parkourEX.util.Pdc;

import java.util.UUID;

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

        String id = Pdc.getString(player, "cp-id");
        if (id != null) CreateTool.removeGame(UUID.fromString(id));

        Services.giveCreateParkour(player);

        return true;
    }
}
