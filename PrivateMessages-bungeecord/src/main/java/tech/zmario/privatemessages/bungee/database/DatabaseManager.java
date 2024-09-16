package tech.zmario.privatemessages.bungee.database;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.byteflux.libby.Library;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tech.zmario.privatemessages.bungee.PrivateMessagesBungee;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DatabaseManager {

    private final PrivateMessagesBungee plugin;
    private HikariDataSource dataSource;
    private Connection connection;

    public DatabaseManager(PrivateMessagesBungee plugin, boolean useMySql) {
        this.plugin = plugin;

        setup(useMySql);
        makeTables();
    }

    private void setup(boolean useMySql) {
        if (useMySql) {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:mysql://" + SettingsConfiguration.MYSQL_HOST.getString() + ":" +
                    SettingsConfiguration.MYSQL_PORT.getInt() + "/" + SettingsConfiguration.MYSQL_DATABASE.getString() + "?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC");
            config.setDriverClassName(SettingsConfiguration.MYSQL_DRIVER.getString());
            config.setUsername(SettingsConfiguration.MYSQL_USERNAME.getString());
            config.setPassword(SettingsConfiguration.MYSQL_PASSWORD.getString());
            config.setPoolName("PrivateMessages");

            dataSource = new HikariDataSource(config);
        } else {
            connection = getConnection();
        }
    }

    private void makeTables() {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `players_data` (uuid varchar(36) NOT NULL, social_spy boolean NOT NULL, toggled_messages boolean NOT NULL)");
                 PreparedStatement preparedStatement1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `ignored_players` (uuid varchar(36) NOT NULL, ignored varchar(36) NOT NULL)")) {

                preparedStatement.executeUpdate();
                preparedStatement1.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to create tables. Error message: " + e.getMessage());
            }
        });
    }

    public boolean isPresent(ProxiedPlayer player) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players_data WHERE uuid = ?")) {
                preparedStatement.setString(1, player.getUniqueId().toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to check if player is present. Error message: " + e.getMessage());
            }
            return false;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return false;
    }

    public void createPlayer(ProxiedPlayer player) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO players_data (uuid, social_spy, toggled_messages) VALUES (?, ?, ?);")) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setBoolean(2, false);
                statement.setBoolean(3, false);

                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to add player to database. Error message: " + e.getMessage());
            }
        });
    }

    public Connection getConnection() {
        try {
            if (dataSource != null) {
                return dataSource.getConnection();
            } else {
                File databaseFile = new File(plugin.getDataFolder(), "data.db");

                if (!databaseFile.exists()) {
                    try {
                        databaseFile.createNewFile();

                    } catch (IOException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe("Failed to create database file. Error message: " + e.getMessage());

                    }
                }

                try {
                    if (connection != null && !connection.isClosed()) {
                        return connection;
                    }

                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());

                    return connection;
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                    plugin.getLogger().severe("Failed to connect to database. Error message: " + e.getMessage());
                }

                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to get connection. Error message: " + e.getMessage());
        }

        return null;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        } else {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to close connection. Error message: " + e.getMessage());
            }
        }
    }

    public List<String> getIgnoredPlayers(ProxiedPlayer player) {
        List<String> ignoredPlayers = Lists.newArrayList();
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT ignored FROM ignored_players INNER JOIN players_data ON players_data.uuid = ignored_players.uuid WHERE players_data.uuid = ?")) {
                preparedStatement.setString(1, player.getUniqueId().toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        ignoredPlayers.add(resultSet.getString("ignored"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to get ignored players. Error message: " + e.getMessage());
            }
        });

        return ignoredPlayers;
    }

    public boolean getToggledStatus(ProxiedPlayer player) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT toggled_messages FROM players_data WHERE uuid = ?")) {
                preparedStatement.setString(1, player.getUniqueId().toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getBoolean("toggled_messages");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to get toggled status. Error message: " + e.getMessage());
            }
            return false;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return false;
    }

    public boolean getSocialSpyStatus(ProxiedPlayer player) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT social_spy FROM players_data WHERE uuid = ?")) {
                preparedStatement.setString(1, player.getUniqueId().toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getBoolean("social_spy");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to get social spy status. Error message: " + e.getMessage());
            }

            return false;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return false;
    }

    public void removeIgnore(ProxiedPlayer player, String ignored) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ignored_players WHERE uuid = ? AND ignored = ?")) {
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, ignored);

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to remove ignore. Error message: " + e.getMessage());
            }
        });
    }

    public void addIgnore(ProxiedPlayer player, String ignored) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ignored_players (uuid, ignored) VALUES (?, ?);")) {
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, ignored);

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to add ignore. Error message: " + e.getMessage());
            }
        });
    }

    public void updateSocialSpy(ProxiedPlayer player, boolean status) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players_data SET social_spy = ? WHERE uuid = ?")) {
                preparedStatement.setBoolean(1, status);
                preparedStatement.setString(2, player.getUniqueId().toString());

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to update social spy. Error message: " + e.getMessage());
            }
        });
    }

    public void updateMessagesToggled(ProxiedPlayer player, boolean status) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players_data SET toggled_messages = ? WHERE uuid = ?")) {
                preparedStatement.setBoolean(1, status);
                preparedStatement.setString(2, player.getUniqueId().toString());

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to update toggled messages. Error message: " + e.getMessage());
            }
        });
    }
}
