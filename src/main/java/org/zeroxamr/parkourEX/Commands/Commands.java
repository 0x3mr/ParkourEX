package org.zeroxamr.parkourEX.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.zeroxamr.parkourEX.Main;

import java.util.HashMap;
import java.util.Set;

public class Commands implements CommandExecutor {
    private static Main plugin = null;
    private static final HashMap<String, Base> commands = new HashMap<>();
    private static final Set<String> RESTRICT_INGAME = Set.of("Checkpoint", "Create", "Cancel", "Reset", "Start");

    public Commands(Main plugin) {
        Commands.plugin = plugin;

        register(new Start());
        register(new Checkpoint());
        register(new Reset());
        register(new Cancel());

        register(new Create());
        register(new Help());
    }

    private void register(Base baseCommand) {
        commands.put(baseCommand.getName().toLowerCase(), baseCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("" + ChatColor.GRAY + "Running version " + ChatColor.GOLD + plugin.getPluginMeta().getVersion());
            sender.sendMessage("" + ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/parkourex help" + ChatColor.GRAY + " to view available commands.");

            return true;
        }

        Base commandExecuted = commands.get(args[0].toLowerCase());

        if (commandExecuted == null) {
            sender.sendMessage("" + ChatColor.GRAY + "Unknown or incomplete command.");
            sender.sendMessage("" + ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/parkourex help" + ChatColor.GRAY + " to view available commands.");

            return true;
        }

        if (!(sender instanceof Player) && RESTRICT_INGAME.contains(commandExecuted.getName())) {
            sender.sendMessage("" + ChatColor.RED + "Command restricted to in-game players only.");
            return true;
        }

        return commandExecuted.execute(sender, args);
    }

    public static Main getPlugin() {
        return plugin;
    }
    public static HashMap<String, Base> getCommands() {
        return commands;
    }
}
