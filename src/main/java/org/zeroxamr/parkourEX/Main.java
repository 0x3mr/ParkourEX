package org.zeroxamr.parkourEX;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {
    private static final HashMap<UUID, ParkourGame> parkourGames = new HashMap<>();
    private static Database DBM = null;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }

        Services.initialize(this);
        Database.initialize(this);
        Utilities.initialize(this);
        ParkourItems.initialize(this);
        Commands.initialize(this);

        ParkourTags.cleanup();

        DBM = new Database();
        DBM.loadGames(parkourGames);

        Utilities.resetPlayersInfo();

        this.getCommand("parkourex").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new ParkourTags(), this);
        getServer().getPluginManager().registerEvents(new ParkourItems(), this);
        getServer().getPluginManager().registerEvents(new Services(), this);
    }

    public ParkourGame getParkourGame(UUID uuid) {
        return parkourGames.get(uuid);
    }

    @Override
    public void onDisable() {
        // Add saved current parkours logic
        // Plugin shutdown logic
        ParkourTags.cleanup();
        parkourGames.clear();
        DBM.shutdown();
    }

    public void reload() {
        getLogger().info("Reloading plugin configs...");
//        DBM.saveGames(parkourGames);
    }

    public static Database getDBM() {
        return DBM;
    }

    public static HashMap<UUID, ParkourGame> getParkourGames() { return parkourGames; }
}
