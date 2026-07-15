package org.zeroxamr.parkourEX.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.game.GameInstance;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.util.Pdc;

public class Start implements Base {
    @Override
    public String getName() {
        return "Start";
    }

    @Override
    public String getInfo() {
        return "Starts the specified parkour";
    }

    @Override
    public String getUsage() {
        return "/pkx start <parkour_id>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cIncorrect command usage.");
            sender.sendMessage("§cUse §e" + getUsage() + "§c.");
            return true;
        }

        int id = 0;
        try {
            id = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage("§cIncorrect command usage.");
            sender.sendMessage("§cEnter a valid number.");
            return true;
        }

        Player player = (Player) sender;

        if (!GameRegistry.hasGame(id)) {
            player.sendMessage("§cParkour not found.");
            player.sendMessage("§cEnter a valid parkour id.");
            return true;
        }

        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))) {
            player.sendMessage("§cYou are currently in a parkour race. Use " + Commands.getCommands().get("Reset".toLowerCase()).getUsage());
            return true;
        }

        GameInstance game = GameRegistry.getParkourGame(id);

        Location location = game.getCheckpointMapWithYaw().firstEntry().getKey();
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);

        Vector direction = location.getDirection();
        direction.setY(0).normalize().multiply(-0.5);
        location.add(direction);

        player.teleport(location);

        game.playerStateStart(player, id);

        player.sendMessage("§a§lParkour challenge started!");
        player.sendMessage("§aUse §e" + Commands.getCommands().get("Checkpoint".toLowerCase()).getUsage() + " §ato teleport to the last checkpoint or §e" + Commands.getCommands().get("Cancel".toLowerCase()).getUsage() + " §ato cancel!");

        return true;
    }
}
