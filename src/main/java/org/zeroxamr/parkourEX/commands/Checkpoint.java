package org.zeroxamr.parkourEX.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.ParkourGame;
import org.zeroxamr.parkourEX.util.Pdc;

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

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))) {
            ParkourGame.playerStateCheckpoint(player);
        }
        else {
            player.sendMessage("§cYou are currently not in a parkour race. Use " + Commands.getCommands().get("Start".toLowerCase()).getUsage());
        }

        return true;
    }
}
