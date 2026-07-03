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

    private HikariDataSource dataSource;
    private final HikariConfig config = new HikariConfig();
    private final String databasePath;

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

        config.setJdbcUrl("jdbc:sqlite:" + databasePath);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

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
                        "checkpoints TEXT NOT NULL," +
                        "parkourCreator TEXT NOT NULL" +
                ");" +
                "CREATE TABLE IF NOT EXISTS Stats (" +
                        "id INTEGER NOT NULL," +
                        "uuid TEXT NOT NULL," +
                        "bestScore INTEGER DEFAULT 0," +
                        "gamesCompleted INTEGER DEFAULT 0," +
                        "PRIMARY KEY (id, uuid)," +
                        "FOREIGN KEY (id) REFERENCES Parkour(id) ON DELETE CASCADE" +
                ");" +
                "CREATE TABLE IF NOT EXISTS Checkpoints (" +
                        "id INTEGER NOT NULL," +
                        "uuid TEXT NOT NULL," +
                        "checkpointID INTEGER NOT NULL," +
                        "score INTEGER NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (id, uuid, checkpointID)," +
                        "FOREIGN KEY (id, uuid) REFERENCES Stats(id, uuid) ON DELETE CASCADE" +
                ");";

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(ParkourTablesQuery)) {
            qst.executeUpdate();
            plugin.getLogger().info("Loaded tables successfully.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load tables: " + e.getMessage());
        }
    }

    public boolean saveGame(LinkedHashMap<Location, Integer> newLocations, String gameCreator) {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to save new game data.");
            return false;
        }

        String newGameSQL =
                "INSERT INTO Parkour (checkpoints, parkourCreator) " +
                        "VALUES (?, ?)" +
                        "RETURNING *";

        String locations = Utilities.serializeLocations(
            newLocations.keySet().stream().toList()
        );

        try (Connection con = this.getConnection();
            PreparedStatement qst = con.prepareStatement(newGameSQL)) {
            qst.setString(1, locations);
            qst.setString(2, gameCreator);

            try (ResultSet res = qst.executeQuery()) {
                if (res.next()) {
                    int id = res.getInt("id");
                    LinkedHashMap<Location, Integer> checkpoints = Utilities.deserializeLocations(res.getString("checkpoints"));
                    String parkourCreator = res.getString("parkourCreator");

                    ParkourGame parkourGame = new ParkourGame(plugin, id, checkpoints, parkourCreator, true);
                    getServer().getPluginManager().registerEvents(parkourGame, plugin);

                    Main.getParkourGames().put(id, parkourGame);
                }
            }
            catch (SQLException e) {
                plugin.getLogger().severe("Failed to return newly saved game: " + e.getMessage());
                return false;
            }
        }
        catch (SQLException e) {
            plugin.getLogger().severe("Failed to save new game data: " + e.getMessage());
            return false;
        }

        return true;
    }

    public void loadGames() {
        String ParkourGamesQuery = "SELECT * FROM Parkour";

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(ParkourGamesQuery);
             ResultSet res = qst.executeQuery()) {
            while (res.next()) {
                int id = res.getInt("id");
                LinkedHashMap<Location, Integer> checkpoints = Utilities.deserializeLocations(res.getString("checkpoints"));
                String parkourCreator = res.getString("parkourCreator");

                ParkourGame parkourGame = new ParkourGame(plugin, id, checkpoints, parkourCreator, false);
                getServer().getPluginManager().registerEvents(parkourGame, plugin);

                Main.getParkourGames().put(id, parkourGame);
            }
        }
        catch (SQLException e) {
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
