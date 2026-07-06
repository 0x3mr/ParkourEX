package org.zeroxamr.parkourEX.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.Main;

public class Cancel implements Base {
    @Override
    public String getName() {
        return "Cancel";
    }

    @Override
    public String getInfo() {
        return "Exits your current parkour session";
    }

    @Override
    public String getUsage() {
        return "/pkx cancel";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (player.hasMetadata("inParkour")
                && player.getMetadata("inParkour").getFirst().asBoolean()
                    && player.hasMetadata("parkourID")) {
            Main.getParkourGames().get(player.getMetadata("parkourID").getFirst().asInt()).playerStateCancel(player);
        }

        player.sendMessage("§c§lParkour challenge cancelled!");

        return true;
    }
}
