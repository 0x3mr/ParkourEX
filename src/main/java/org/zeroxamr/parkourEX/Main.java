package org.zeroxamr.parkourEX;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {
    private static final HashMap<UUID, ParkourGame> parkourGames = new HashMap<>();
    private static Database DBM = null;

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        Services.initialize(this);
        Database.initialize(this);
        Utilities.initialize(this);
        ParkourItems.initialize(this);
        Commands.initialize(this);

        DBM = new Database();
//        DBM.fakeInsert();
//        DBM.fakeInsert2();
        DBM.loadGames(parkourGames);

        Utilities.resetPlayersInfo();

        this.getCommand("psetup").setExecutor(new Commands());
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
