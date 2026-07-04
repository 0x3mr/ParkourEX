package org.zeroxamr.parkourEX.Commands;

import org.bukkit.command.CommandSender;

public interface Base {
    String getName();
    String getInfo();
    String getUsage();
    boolean execute(CommandSender sender, String[] args);
}
