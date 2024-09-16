package tech.zmario.privatemessages.bungee;

import lombok.Getter;
import net.byteflux.libby.BungeeLibraryManager;
import net.byteflux.libby.Library;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import tech.zmario.privatemessages.bungee.commands.*;
import tech.zmario.privatemessages.bungee.configuration.ConfigManager;
import tech.zmario.privatemessages.bungee.database.DatabaseManager;
import tech.zmario.privatemessages.bungee.enums.SettingsConfiguration;
import tech.zmario.privatemessages.bungee.listeners.BaseListeners;
import tech.zmario.privatemessages.common.configuration.Configuration;
import tech.zmario.privatemessages.common.storage.DataStorage;

@Getter
public final class PrivateMessagesBungee extends Plugin {

    @Getter
    private static PrivateMessagesBungee instance;
    private ConfigManager configManager;
    private BungeeLibraryManager libraryManager;
    private DataStorage storage;
    private DatabaseManager databaseManager;
    private BungeeAudiences adventure;

    @Override
    public void onEnable() {
        setupConfigurations();
        setupInstances();
        registerListeners(new BaseListeners(this));
        registerCommands(new MessageCommand(this), new ReplyCommand(this), new IgnoreCommand(this),
                new SocialSpyCommand(this), new ToggleMessagesCommand(this));
    }

    private void registerCommands(Command... commands) {
        for (Command command : commands) {
            getProxy().getPluginManager().registerCommand(this, command);
        }
    }

    private void setupConfigurations() {
        configManager = new ConfigManager(this);

        configManager.create("config.yml");
        configManager.create("messages.yml");
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners)
            getProxy().getPluginManager().registerListener(this, listener);
    }

    private void setupInstances() {
        instance = this;
        storage = new DataStorage();

        libraryManager = new BungeeLibraryManager(this);
        libraryManager.addMavenCentral();

        boolean useMySql = SettingsConfiguration.MYSQL_ENABLED.getBoolean();

        loadLibraries(useMySql);

        databaseManager = new DatabaseManager(this, useMySql);
        adventure = BungeeAudiences.create(this);
    }

    private void loadLibraries(boolean useMySql) {
        if (useMySql) {
            Library hikariCp = Library.builder().groupId("com{}zaxxer").artifactId("HikariCP").version("4.0.3").build();
            Library mysqlConnector = Library.builder().groupId("mysql").artifactId("mysql-connector-java").version("8.0.19").build();

            libraryManager.loadLibrary(hikariCp);
            libraryManager.loadLibrary(mysqlConnector);
        } else {
            Library sqLite = Library.builder().groupId("org{}xerial").artifactId("sqlite-jdbc").version("3.36.0.3").build();

            libraryManager.loadLibrary(sqLite);
        }
    }

    @Override
    public void onDisable() {
        configManager = null;
        storage = null;
        databaseManager.close();
        databaseManager = null;
        instance = null;

        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public Configuration getConfig() {
        return configManager.get("config.yml");
    }

    public Configuration getMessages() {
        return configManager.get("messages.yml");
    }
}