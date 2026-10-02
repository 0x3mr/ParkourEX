package org.zeroxamr.parkourEX.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zeroxamr.parkourEX.Main;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.game.models.CommandExecutor;
import org.zeroxamr.parkourEX.game.models.CommandMeta;
import org.zeroxamr.parkourEX.listeners.GameItems;

import java.io.File;

public class ConfigManager {
    private static YamlConfiguration commandsConfig;

    public static void loadConfig() {
        Main.getPlugin().saveDefaultConfig();
        Main.getPlugin().getConfig().options().copyDefaults(true);
        Main.getPlugin().saveConfig();

        Config.load();
    }

    public static void loadCommandsConfig() {
        Main.getPlugin().saveResource("commands.yml", false);

        commandsConfig = YamlConfiguration.loadConfiguration(
                new File(Main.getPlugin().getDataFolder(), "commands.yml")
        );

        loadEventCommands("onParkourStart");
        loadEventCommands("onParkourEnd");
        loadEventCommands("onParkourExit");
    }

    public static void reloadPlugin() {
        Config.load();
        GameItems.createItems();
        GameRegistry.clearCommands();
        loadCommandsConfig();
    }

    public static void loadEventCommands(String eventName) {
        ConfigurationSection config = commandsConfig.getConfigurationSection(eventName);
        if (config == null) return;

        for (String event : config.getKeys(false)) {
            ConfigurationSection eventSection = config.getConfigurationSection(event);

            if (eventSection == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse section " + event + " of " + eventName);
                continue;
            }

            String command = eventSection.getString("command");

            if (command == null) {
                Main.getPlugin().getLogger().info(" - Failed to parse command of section " + eventSection.getName() + " of " + eventName);
                continue;
            }

            CommandExecutor executor;

            try {
                executor = CommandExecutor.valueOf(eventSection.getString("executor", "").toUpperCase());
            }
            catch (IllegalArgumentException e) {
                Main.getPlugin().getLogger().info(" - Failed to parse executor of section " + eventSection.getName() + " of " + eventName);
                continue;
            }

            int id = eventSection.getInt("id", -1);
            long delay = Math.max(eventSection.getLong("delay"), 0);

            GameRegistry.addCommand(
                    eventName,
                    id,
                    new CommandMeta(command, executor, delay)
            );
        }
    }
}
