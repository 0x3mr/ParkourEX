package org.zeroxamr.parkourEX.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Help implements Base {
    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public String getInfo() {
        return "Lists all available parkour commands";
    }

    @Override
    public String getUsage() {
        return "/pkx help";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cIncorrect command usage.");
            sender.sendMessage("§cUse §e" + getUsage() + "§c.");
            return true;
        }

        StringBuilder helpMessage = new StringBuilder(" " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ParkourEx" + ChatColor.RESET + ChatColor.GRAY + " - List of commands:");

        for (Base command : Commands.getCommands().values()) {
            helpMessage.append("\n   " + ChatColor.GRAY + "- " + ChatColor.WHITE + command.getUsage() + ChatColor.LIGHT_PURPLE + " - " + command.getInfo());
        }

        sender.sendMessage(String.valueOf(helpMessage));

        return true;
    }
}
