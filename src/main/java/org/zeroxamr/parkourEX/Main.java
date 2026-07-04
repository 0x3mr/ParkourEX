package org.zeroxamr.parkourEX;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroxamr.parkourEX.Commands.Commands;

import java.io.File;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {
    private static final HashMap<Integer, ParkourGame> parkourGames = new HashMap<>();
    private static Database DBM = null;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        getConfig().options().copyDefaults(true);
        saveConfig();

        Services.initialize(this);
        Database.initialize(this);
        Utilities.initialize(this);
        ParkourItems.initialize(this);

        DBM = new Database();

        ParkourTags.cleanup();
        Utilities.resetPlayersInfo();

        this.getCommand("parkourex").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new ParkourTags(), this);
        getServer().getPluginManager().registerEvents(new ParkourItems(), this);
        getServer().getPluginManager().registerEvents(new Services(), this);

        DBM.loadGames();

        ParkourTags.loadTags();
    }

    public ParkourGame getParkourGame(Integer uuid) {
        return parkourGames.get(uuid);
    }

    @Override
    public void onDisable() {
        ParkourTags.cleanup();
        parkourGames.clear();
        DBM.shutdown();
    }

    public static Database getDBM() {
        return DBM;
    }

    public static HashMap<Integer, ParkourGame> getParkourGames() {
        return parkourGames;
    }
}
