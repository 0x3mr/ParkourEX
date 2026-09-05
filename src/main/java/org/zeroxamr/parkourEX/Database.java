package org.zeroxamr.parkourEX;

import com.google.common.math.Stats;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.zeroxamr.parkourEX.game.GameHolograms;
import org.zeroxamr.parkourEX.game.GameInstance;
import org.zeroxamr.parkourEX.game.GameRegistry;
import org.zeroxamr.parkourEX.game.StatsRegistry;
import org.zeroxamr.parkourEX.game.models.PlayerMeta;
import org.zeroxamr.parkourEX.util.Shared;

import java.io.File;
import java.sql.*;
import java.util.*;

public class Database {
    private static Main plugin = null;
    private final String databasePath;
    private HikariDataSource dataSource;
    private final HikariConfig config = new HikariConfig();

    public static void initialize(Main plugin) {
        Database.plugin = plugin;
    }

    Database() {
        File folderPath = plugin.getDataFolder();

        if (!folderPath.exists()) {
            if (!folderPath.mkdirs()) {
                plugin.getLogger().info("Failed to create database files.");
            }
        }

        databasePath = new File(folderPath, "database.db").getAbsolutePath();

        connect();
        setupTables();
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

        if (isOnline()) {
            plugin.getLogger().info("Connected to SQLite successfully.");
            return;
        }

        plugin.getLogger().info("Failed to connect to SQLite.");
    }

    public void setupTables() {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to load saved data.");
            return;
        }

        String parkourGamesTable =
                "CREATE TABLE IF NOT EXISTS Parkour (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "checkpoints TEXT NOT NULL," +
                        "parkourCreator TEXT NOT NULL" +
                ");";

        String playerStatisticsTable =
                "CREATE TABLE IF NOT EXISTS Stats (" +
                        "id INTEGER NOT NULL," +
                        "uuid TEXT NOT NULL," +
                        "bestScore INTEGER DEFAULT 0," +
                        "PRIMARY KEY (id, uuid)," +
                        "FOREIGN KEY (id) REFERENCES Parkour(id) ON DELETE CASCADE" +
                ");";

        String perGameCheckpointsTable =
                "CREATE TABLE IF NOT EXISTS Checkpoints (" +
                        "id INTEGER NOT NULL," +
                        "uuid TEXT NOT NULL," +
                        "checkpointID INTEGER NOT NULL," +
                        "duration INTEGER NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (id, uuid, checkpointID)," +
                        "FOREIGN KEY (id, uuid) REFERENCES Stats(id, uuid) ON DELETE CASCADE" +
                ");";

        try (Connection con = this.getConnection();
             Statement statement = con.createStatement()) {
            statement.executeUpdate(parkourGamesTable);
            statement.executeUpdate(playerStatisticsTable);
            statement.executeUpdate(perGameCheckpointsTable);

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

        String locations = Shared.serializeLocations(
            newLocations.keySet().stream().toList()
        );

        try (Connection con = this.getConnection();
            PreparedStatement qst = con.prepareStatement(newGameSQL)) {
            qst.setString(1, locations);
            qst.setString(2, gameCreator);

            try (ResultSet res = qst.executeQuery()) {
                if (res.next()) {
                    int id = res.getInt("id");
                    LinkedHashMap<Location, Integer> checkpoints = Shared.deserializeLocations(res.getString("checkpoints"));
                    String parkourCreator = res.getString("parkourCreator");

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        GameRegistry.registerGame(
                                id,
                                new GameInstance(plugin, id, checkpoints, parkourCreator),
                                checkpoints
                        );

                        GameHolograms.register(id, checkpoints);
                        GameHolograms.resync();
                    });
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

    public void flushPlayersData() {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to save players data.");
            return;
        }

        try (Connection con = this.getConnection()) {
            String playerMetaSQL =
                    "INSERT INTO Stats (id, uuid, bestScore) VALUES (?, ?, ?) " +
                            "ON CONFLICT (id, uuid) DO UPDATE " +
                            "SET bestScore = EXCLUDED.bestScore " +
                            "WHERE bestScore > EXCLUDED.bestScore";

            try (PreparedStatement qst = con.prepareStatement(playerMetaSQL)) {
                for (Map.Entry<String, PlayerMeta> player : StatsRegistry.getPlayerStatisticsTable().entrySet()) {
                    String[] parts = player.getKey().split(";");

                    int id = Integer.parseInt(parts[0]);
                    String uuid = parts[1];
                    long bestScore = player.getValue().bestScore();

                    qst.setInt(1, id);
                    qst.setString(2, uuid);
                    qst.setLong(3, bestScore);

                    qst.executeUpdate();
                }
            }

            String checkpointsSQL =
                    "INSERT INTO Checkpoints (id, uuid, checkpointID, duration) VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT (id, uuid, checkpointID) DO UPDATE " +
                            "SET duration = EXCLUDED.duration " +
                            "WHERE duration > EXCLUDED.duration";

            try (PreparedStatement qst = con.prepareStatement(checkpointsSQL)) {
                for (Map.Entry<String, Long> checkpoint : StatsRegistry.getPerGameCheckpointsTable().entrySet()) {
                    String[] parts = checkpoint.getKey().split(";");

                    int id = Integer.parseInt(parts[0]);
                    String uuid = parts[1];
                    int checkpointNumber = Integer.parseInt(parts[2]);
                    long score = checkpoint.getValue();

                    qst.setInt(1, id);
                    qst.setString(2, uuid);
                    qst.setInt(3, checkpointNumber);
                    qst.setLong(4, score);

                    qst.executeUpdate();
                }
            }
        } catch (SQLException error) {
            plugin.getLogger().severe("Failed to flush player data to disk: " + error.getMessage());
        }
    }

    public void retrieveAndPopulatePlayerData(String uuid) {
        if (!isOnline()) {
            plugin.getLogger().info("Database is offline! Failed to retrieve player data.");
            return;
        }

        try (Connection con = this.getConnection()) {
            String playerMetaSQL = "SELECT * FROM Stats WHERE uuid = ?";
            try (PreparedStatement qst = con.prepareStatement(playerMetaSQL)) {
                qst.setString(1, uuid);
                try (ResultSet res = qst.executeQuery()) {
                    while (res.next()) {
                        int id = res.getInt("id");
                        long bestScore = res.getLong("bestScore");
                        String playerID = id + ";" + uuid;
                        StatsRegistry.diffPlayerMeta(playerID, bestScore);
                    }
                }
            }

            String checkpointsSQL = "SELECT * FROM Checkpoints WHERE uuid = ?";
            try (PreparedStatement qst = con.prepareStatement(checkpointsSQL)) {
                qst.setString(1, uuid);
                try (ResultSet res = qst.executeQuery()) {
                    while (res.next()) {
                        int id = res.getInt("id");
                        int checkpointNumber = res.getInt("checkpointID");
                        long duration = res.getLong("duration");
                        String checkpointID = id + ";" + uuid + ";" + checkpointNumber;
                        StatsRegistry.diffCheckpointStats(checkpointID, duration);
                    }
                }
            }
        } catch (SQLException error) {
            plugin.getLogger().severe("Failed to retrieve player saved data: " + error.getMessage());
        }
    }

    public void loadGames() {
        String ParkourGamesQuery = "SELECT * FROM Parkour";

        try (Connection con = this.getConnection();
             PreparedStatement qst = con.prepareStatement(ParkourGamesQuery);
             ResultSet res = qst.executeQuery()) {
            while (res.next()) {
                int id = res.getInt("id");
                LinkedHashMap<Location, Integer> checkpoints = Shared.deserializeLocations(res.getString("checkpoints"));
                String parkourCreator = res.getString("parkourCreator");

                GameRegistry.registerGame(
                        id,
                        new GameInstance(plugin, id, checkpoints, parkourCreator),
                        checkpoints
                );

                GameHolograms.register(id, checkpoints);
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
