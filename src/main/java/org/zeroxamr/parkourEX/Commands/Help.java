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
        return "/parkourex help";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("" + ChatColor.RED + "Incorrect command usage.");
            sender.sendMessage("" + ChatColor.RED + "Use " + ChatColor.YELLOW + getUsage() + ChatColor.RED + ".");
            return true;
        }

        StringBuilder helpMessage = new StringBuilder(" " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "ParkourEx" + ChatColor.RESET + ChatColor.GRAY + " - List of commands:");

        for (Base command : Commands.getCommands().values()) {
            helpMessage.append("\n   " + ChatColor.GRAY + "- " + ChatColor.WHITE + command.getUsage());
        }

        sender.sendMessage(String.valueOf(helpMessage));

        return true;
    }
}
