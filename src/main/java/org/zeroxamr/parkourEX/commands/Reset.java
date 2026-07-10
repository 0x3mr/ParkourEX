package org.zeroxamr.parkourEX.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.util.Pdc;

public class Reset implements Base {
    @Override
    public String getName() {
        return "Reset";
    }

    @Override
    public String getInfo() {
        return "Teleports you to the start line";
    }

    @Override
    public String getUsage() {
        return "/pkx reset [parkour_id]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        int id;
        if (Boolean.TRUE.equals(Pdc.getBoolean(player, "inParkour"))) {
            id = Pdc.getInt(player, "parkourID");
        }
        else {
            if (args.length != 2) {
                sender.sendMessage("§cIncorrect command usage.");
                sender.sendMessage("§cUse §e" + getUsage() + "§c.");
                return true;
            }

            try {
                id = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage("§cIncorrect command usage.");
                sender.sendMessage("§cEnter a valid number.");
                return true;
            }
        }

        if (!GameRegistry.hasGame(id)) {
            player.sendMessage("§cParkour not found.");
            player.sendMessage("§cEnter a valid parkour id.");
            return true;
        }

        Location location = GameRegistry.getParkourGames().get(id).getCheckpointMapWithYaw().firstEntry().getKey();
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);

        Vector direction = location.getDirection();
        direction.setY(0).normalize().multiply(-1.5);
        location.add(direction);

        player.teleport(location);

        if (!Pdc.has(player, "inParkour")
                || Boolean.FALSE.equals(Pdc.getBoolean(player, "inParkour"))) {
            player.sendMessage("§a§lTeleported you to the start of the parkour!");
        }

        return true;
    }
}
