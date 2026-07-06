package org.zeroxamr.parkourEX.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.ParkourGame;
import org.zeroxamr.parkourEX.Utilities;

public class Checkpoint implements Base {
    @Override
    public String getName() {
        return "Checkpoint";
    }

    @Override
    public String getInfo() {
        return "Teleports you to your last checkpoint";
    }

    @Override
    public String getUsage() {
        return "/pkx checkpoint";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (player.hasMetadata("inParkour")
                && player.hasMetadata("checkpointNumber")
                && player.hasMetadata("checkpointLocation")) {
            ParkourGame.playerStateCheckpoint(player);
        }
        else {
            player.sendMessage("§cYou are currently not in a parkour race. Use " + Commands.getCommands().get("Start".toLowerCase()).getUsage());
        }

        return true;
    }
}
