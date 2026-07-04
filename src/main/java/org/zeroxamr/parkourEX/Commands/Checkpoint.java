package org.zeroxamr.parkourEX.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
            Location location = Utilities.deserializeLocation(player.getMetadata("checkpointLocation").getFirst().asString());
            location.setX(location.getX() + 0.5);
            location.setZ(location.getZ() + 0.5);
            player.teleport(location);
        }
        else {
            player.sendMessage("" + ChatColor.RED + "You are not in a parkour!");
        }

        return true;
    }
}
