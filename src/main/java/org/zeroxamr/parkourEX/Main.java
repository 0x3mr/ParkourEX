package org.zeroxamr.parkourEX;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroxamr.parkourEX.commands.Commands;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.game.GameHolograms;
import org.zeroxamr.parkourEX.listeners.ChunkHandler;
import org.zeroxamr.parkourEX.listeners.CreateTool;
import org.zeroxamr.parkourEX.listeners.GameListener;
import org.zeroxamr.parkourEX.listeners.GameItems;
import org.zeroxamr.parkourEX.util.Pdc;
import org.zeroxamr.parkourEX.util.Shared;

import java.io.File;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {
    private static Database DBM = null;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        getConfig().options().copyDefaults(true);
        saveConfig();

        Pdc.initialize(this);
        Shared.initialize(this);
        Services.initialize(this);
        Database.initialize(this);
        GameItems.initialize(this);

        GameHolograms.cleanup();
        Shared.resetPlayersInfo();

        DBM = new Database();

        Objects.requireNonNull(this.getCommand("ParkourEX".toLowerCase())).setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new ChunkHandler(), this);
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getServer().getPluginManager().registerEvents(new CreateTool(), this);
        getServer().getPluginManager().registerEvents(new GameItems(), this);
        getServer().getPluginManager().registerEvents(new Services(), this);

        DBM.loadGames();

        GameHolograms.loadTags();
    }

    @Override
    public void onDisable() {
        GameHolograms.cleanup();
        GameRegistry.cleanup();
        DBM.shutdown();
    }

    public static Database getDBM() {
        return DBM;
    }
}
