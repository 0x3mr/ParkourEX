package org.zeroxamr.parkourEX.commands;

import org.bukkit.command.CommandSender;

public interface Base {
    String getName();
    String getInfo();
    String getUsage();
    boolean execute(CommandSender sender, String[] args);
}
