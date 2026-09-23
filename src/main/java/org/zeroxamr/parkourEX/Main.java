package org.zeroxamr.parkourEX;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroxamr.parkourEX.commands.Commands;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.game.GameHolograms;
import org.zeroxamr.parkourEX.game.GameSaver;
import org.zeroxamr.parkourEX.game.StatsRegistry;
import org.zeroxamr.parkourEX.listeners.ChunkHandler;
import org.zeroxamr.parkourEX.listeners.CreateTool;
import org.zeroxamr.parkourEX.listeners.GameListener;
import org.zeroxamr.parkourEX.listeners.GameItems;
import org.zeroxamr.parkourEX.util.Shared;

import java.io.File;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {
    private static Main plugin = null;
    private static Database DBM = null;

    @Override
    public void onEnable() {
        plugin = this;
        DBM = new Database();

        ConfigManager.loadConfig();
        GameItems.createItems();
        GameHolograms.cleanup();
        Shared.resetPlayersInfo();

        Objects.requireNonNull(this.getCommand("ParkourEX".toLowerCase())).setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new ChunkHandler(), this);
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getServer().getPluginManager().registerEvents(new CreateTool(), this);
        getServer().getPluginManager().registerEvents(new GameItems(), this);
        getServer().getPluginManager().registerEvents(new Services(), this);

        DBM.loadGames();

        GameHolograms.loadTags();
        GameSaver.startScheduler();
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static Database getDBM() {
        return DBM;
    }

    @Override
    public void onDisable() {
        GameSaver.stopScheduler();

        GameHolograms.cleanup();
        GameRegistry.cleanup();

        Main.getDBM().flushPlayersData();
        DBM.shutdown();
    }
}
