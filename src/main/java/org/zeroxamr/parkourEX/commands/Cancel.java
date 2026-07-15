package org.zeroxamr.parkourEX.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.util.Pdc;

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

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))) {
            int gameID = Pdc.getInt(player, "parkourID");
            GameRegistry.getParkourGame(gameID).playerStateCancel(player);
            player.sendMessage("§c§lParkour challenge cancelled!");
        }
        else {
            player.sendMessage("§cYou are currently not in a parkour race. Use " + Commands.getCommands().get("Start".toLowerCase()).getUsage());
        }

        return true;
    }
}
