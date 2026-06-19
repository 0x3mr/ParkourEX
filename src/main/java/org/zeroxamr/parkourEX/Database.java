package org.zeroxamr.parkourEX;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Database {
    private static Main plugin = null;
    public static void initialize(Main plugin) {
        Database.plugin = plugin;
    }

    HikariDataSource dataSource;
    HikariConfig config = new HikariConfig();
    String databasePath;

    Database() {
        File folderPath = plugin.getDataFolder();
        if (!folderPath.exists()) {
            if (!folderPath.mkdirs()) {
                plugin.getLogger().info("Failed to create database files.");
            }
        }

        databasePath = new File(folderPath, "database.db").getAbsolutePath();

        this.connect();
        this.loadTables();
    }

    public void connect() {
        if (this.isOnline()) {
            plugin.getLogger().info("Already connected to database!");
            return;
        }

        config.setJdbcUrl("JDBC:sqlite:" + databasePath);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);

        this.dataSource = new HikariDataSource(config);

        if (this.isOnline()) {
            plugin.getLogger().info("Connected to SQLite successfully.");
            return;
        }

        plugin.getLogger().info("Failed to connect to SQLite.");
    }

    public void loadTables() {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to load saved data.");
            return;
        }

        String ParkourTablesQuery =
        "CREATE TABLE IF NOT EXISTS Parkour (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "world TEXT NOT NULL," +
            "checkpoints TEXT NOT NULL," +
            "checkpointsAmount INTEGER NOT NULL," +
            "parkourCreator TEXT NOT NULL" +
        ")";

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(ParkourTablesQuery)) {
            qst.executeUpdate();
            plugin.getLogger().info("Loaded tables successfully.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load tables: " + e.getMessage());
        }
    }

    public void fakeInsert() {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to insert fake data.");
            return;
        }

        String fakeSQL =
        "INSERT INTO Parkour (world, checkpoints, checkpointsAmount, parkourCreator)" +
        "VALUES (?, ?, ?, ?)";

        World world = Bukkit.getWorld("test");
        Location start = new Location(world, -3, -60, 3, 45, 0);
        Location one = new Location(world, -6, -60, 6, 90, 0);
        Location two = new Location(world, -10, -60, 6, 135, 0);
        Location end = new Location(world, -12, -60, 3, 0, 0);
        List<Location> locations = new ArrayList<>(List.of(start, one, two, end));

        String serializedCheckpoints = Utilities.serializeLocations(locations);

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(fakeSQL)) {
            qst.setString(1, start.getWorld().getName());
            qst.setString(2, serializedCheckpoints);
            qst.setInt(3, locations.size());
            qst.setString(4, "0x3mr");
            qst.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert fake data: " + e.getMessage());
        }
    }

    public void fakeInsert2() {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to insert fake data.");
            return;
        }

        String fakeSQL =
        "INSERT INTO Parkour (world, checkpoints, checkpointsAmount, parkourCreator)" +
        "VALUES (?, ?, ?, ?)";

        World world = Bukkit.getWorld("test");
        Location start = new Location(world, -19, -60, -8, 45, 0);
        Location one = new Location(world, -24, -60, -8, 90, 0);
        Location two = new Location(world, -25, -60, -4, 135, 0);
        Location end = new Location(world, -21, -60, -1, 0, 0);
        List<Location> locations = new ArrayList<>(List.of(start, one, two, end));

        String serializedCheckpoints = Utilities.serializeLocations(locations);

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(fakeSQL)) {
            qst.setString(1, start.getWorld().getName());
            qst.setString(2, serializedCheckpoints);
            qst.setInt(3, locations.size());
            qst.setString(4, "0x3mr");
            qst.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert fake data: " + e.getMessage());
        }
    }

    public void saveGame(ParkourGame game, LinkedHashMap<Location, Integer> incomingLocations) {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to save new game data.");
            return;
        }

        String newGameSQL =
            "INSERT INTO Parkour (world, checkpoints, checkpointsAmount, parkourCreator) " +
            "VALUES (?, ?, ?, ?)";

        List<Location> locations = new ArrayList<>(incomingLocations.keySet());

        int checkpointsAmount = locations.size();

        String world = locations.getFirst().getWorld().getName();
        String checkpoints = Utilities.serializeLocations(locations);
        String parkourCreator = game.getGameAdmin();

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(newGameSQL)) {
            qst.setString(1, world);
            qst.setString(2, checkpoints);
            qst.setInt(3, checkpointsAmount);
            qst.setString(4, parkourCreator);
            qst.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save new game data: " + e.getMessage());
        }
    }

    public void loadGames(HashMap<UUID, ParkourGame> parkourGames) {
        String ParkourGamesQuery = "SELECT * FROM Parkour";

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(ParkourGamesQuery);
             ResultSet res = qst.executeQuery()) {
            while (res.next()) {
//                int id = res.getInt("id");
//                String world = res.getString("world");
                LinkedHashMap<Location, Integer> parkourLocations = Utilities.deserializeLocations(res.getString("checkpoints"));
//                int checkpointsAmount = res.getInt("checkpointsAmount");
//                String parkourCreator = res.getString("parkourCreator");

                UUID uuid = Utilities.generateRandomID();
                ParkourGame parkourGame = new ParkourGame(plugin, uuid, parkourLocations);
                getServer().getPluginManager().registerEvents(parkourGame, plugin);
                parkourGames.put(uuid, parkourGame);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve saved parkour games: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Boolean isOnline() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void shutdown() {
        if (isOnline()) {
            dataSource.close();
        }
    }
}
