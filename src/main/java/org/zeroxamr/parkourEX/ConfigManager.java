package org.zeroxamr.parkourEX;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.game.models.CommandExecutor;
import org.zeroxamr.parkourEX.game.models.CommandMeta;

import java.io.File;

public class ConfigManager {
    private static FileConfiguration commandsConfig;

    public static void initialize() {
        File file = new File(Main.getPlugin().getDataFolder(), "commands.yml");

        if (!file.exists()) {
            Main.getPlugin().saveResource("commands.yml", false);
        }

        commandsConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static void loadExitCommands() {
        String eventName = "onParkourExit";
        ConfigurationSection config = commandsConfig.getConfigurationSection(eventName);
        if (config == null) return;

        for (String event : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(event);
            if (section == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse section " + event + " of " + eventName);
                continue;
            }

            String command = section.getString("command");
            if (command == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse command of section " + section.getName() + " of " + eventName);
                continue;
            }

            CommandExecutor executor;
            try {
                executor = CommandExecutor.valueOf(
                        section.getString("executor", "").toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                Main.getPlugin().getLogger().info(" - Failed to parse executor of section " + section.getName() + " of " + eventName);
                continue;
            }

            long delay = section.getLong("delay");
            if (delay < 0) delay = 0;

            int id = section.getInt("id", -1);

            CommandMeta cmd = new CommandMeta(command, executor, delay);

            if (id == -1) {
                GameRegistry.addExitCommandToAll(cmd);
            } else {
                GameRegistry.addExitCommand(id, cmd);
            }
        }
    }

    public static void loadFinishCommands() {
        String eventName = "onParkourEnd";
        ConfigurationSection config = commandsConfig.getConfigurationSection(eventName);
        if (config == null) return;

        for (String event : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(event);
            if (section == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse section " + event + " of " + eventName);
                continue;
            }

            String command = section.getString("command");
            if (command == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse command of section " + section.getName() + " of " + eventName);
                continue;
            }

            CommandExecutor executor;
            try {
                executor = CommandExecutor.valueOf(
                        section.getString("executor", "").toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                Main.getPlugin().getLogger().info(" - Failed to parse executor of section " + section.getName() + " of " + eventName);
                continue;
            }

            long delay = section.getLong("delay");
            if (delay < 0) delay = 0;

            int id = section.getInt("id", -1);

            CommandMeta cmd = new CommandMeta(command, executor, delay);

            if (id == -1) {
                GameRegistry.addFinishCommandToAll(cmd);
            } else {
                GameRegistry.addFinishCommand(id, cmd);
            }
        }
    }

    public static void loadStartCommands() {
        String eventName = "onParkourStart";
        ConfigurationSection config = commandsConfig.getConfigurationSection(eventName);
        if (config == null) return;

        for (String event : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(event);
            if (section == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse section " + event + " of " + eventName);
                continue;
            }

            String command = section.getString("command");
            if (command == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse command of section " + section.getName() + " of " + eventName);
                continue;
            }

            CommandExecutor executor;
            try {
                executor = CommandExecutor.valueOf(
                        section.getString("executor", "").toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                Main.getPlugin().getLogger().info(" - Failed to parse executor of section " + section.getName() + " of " + eventName);
                continue;
            }

            long delay = section.getLong("delay");
            if (delay < 0) delay = 0;

            int id = section.getInt("id", -1);

            CommandMeta cmd = new CommandMeta(command, executor, delay);

            if (id == -1) {
                GameRegistry.addStartCommandToAll(cmd);
            } else {
                GameRegistry.addStartCommand(id, cmd);
            }
        }
    }
}
