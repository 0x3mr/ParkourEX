package org.zeroxamr.parkourEX.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.Services;
import org.zeroxamr.parkourEX.config.ConfigManager;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.listeners.CreateTool;
import org.zeroxamr.parkourEX.util.Pdc;

import java.util.UUID;

public class Reload implements Base {
    @Override
    public String getName() {
        return "Reload";
    }

    @Override
    public String getInfo() {
        return "/parkour reload";
    }

    @Override
    public String getUsage() {
        return "Reloads the plugin config files";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!sender.isOp()) {
            player.sendMessage("" + ChatColor.RED + "No permission.");
            return true;
        }

        ConfigManager.reloadPlugin();
        player.sendMessage("" + ChatColor.GREEN + "Parkour plugin reloaded successfully.");

        return true;
    }
}
