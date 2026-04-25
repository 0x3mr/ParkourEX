package org.zeroxamr.parkourEX;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin implements Listener {
    private static final HashMap<UUID, ParkourGame> parkourGames = new HashMap<>();

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        Services.initialize(this);
        Database.initialize(this);
        Utilities.initialize(this);
        ParkourItems.initialize(this);
        Commands.initialize(this);

        Database DBM = new Database();
//        DBM.fakeInsert();
//        DBM.fakeInsert2();
        DBM.loadGames(parkourGames);

        Utilities.resetPlayersInfo();

        this.getCommand("psetup").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new ParkourItems(), this);
    }

    public ParkourGame getParkourGame(UUID uuid) {
        return parkourGames.get(uuid);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

//    public static HashMap<UUID, ParkourGame> getParkourGames() { return parkourGames; }
}
