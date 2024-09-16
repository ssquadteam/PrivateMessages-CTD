package tech.zmario.privatemessages.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;
import tech.zmario.privatemessages.common.configuration.Configuration;
import tech.zmario.privatemessages.common.storage.DataStorage;
import tech.zmario.privatemessages.velocity.commands.*;
import tech.zmario.privatemessages.velocity.configuration.ConfigManager;
import tech.zmario.privatemessages.velocity.database.DatabaseManager;
import tech.zmario.privatemessages.velocity.enums.SettingsConfiguration;
import tech.zmario.privatemessages.velocity.listeners.BaseListeners;

@Plugin(
    id = "privatemessages",
    name = "PrivateMessages",
    version = "2.1",
    authors = { "zMario" },
    description = "A plugin that allows you to send private messages to other players."
)
@Getter
public class PrivateMessagesVelocity {

    @Getter
    private static PrivateMessagesVelocity instance;

    private final ProxyServer proxyServer;
    private final Logger logger;

    private ConfigManager configManager;
    private VelocityLibraryManager<PrivateMessagesVelocity> libraryManager;
    private DataStorage storage;
    private DatabaseManager databaseManager;

    @Setter
    private File pluginFolder;

    @Inject
    public PrivateMessagesVelocity(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PluginDescription pluginDescription = getProxyServer()
            .getPluginManager()
            .getPlugin("privatemessages")
            .get()
            .getDescription();

        setupConfigurations(pluginDescription);
        setupInstances();
        registerListeners(new BaseListeners(this));
        registerCommands();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        databaseManager.close();
        databaseManager = null;
        storage = null;
        libraryManager = null;
        configManager = null;
        instance = null;
    }

    private void setupConfigurations(PluginDescription pluginDescription) {
        configManager = new ConfigManager(this, pluginDescription);

        configManager.create("config.yml");
        configManager.create("messages.yml");
    }

    private void setupInstances() {
        instance = this;
        storage = new DataStorage();

        libraryManager = new VelocityLibraryManager<>(
            logger,
            pluginFolder.toPath(),
            getProxyServer().getPluginManager(),
            this
        );
        libraryManager.addMavenCentral();

        boolean useMySql = SettingsConfiguration.MYSQL_ENABLED.getBoolean();

        loadLibraries(useMySql);

        databaseManager = new DatabaseManager(this, useMySql);
    }

    private void registerListeners(Object... listeners) {
        for (Object listener : listeners) getProxyServer()
            .getEventManager()
            .register(this, listener);
    }

    private void registerCommands() {
        proxyServer
            .getCommandManager()
            .register(
                proxyServer
                    .getCommandManager()
                    .metaBuilder(
                        SettingsConfiguration.COMMAND_MESSAGE_NAME.getString()
                    )
                    .aliases(
                        SettingsConfiguration.COMMAND_MESSAGE_ALIASES.getStringList()
                            .toArray(new String[0])
                    )
                    .build(),
                new MessageCommand(this)
            );

        proxyServer
            .getCommandManager()
            .register(
                proxyServer
                    .getCommandManager()
                    .metaBuilder(
                        SettingsConfiguration.COMMAND_REPLY_NAME.getString()
                    )
                    .aliases(
                        SettingsConfiguration.COMMAND_REPLY_ALIASES.getStringList()
                            .toArray(new String[0])
                    )
                    .build(),
                new ReplyCommand(this)
            );

        proxyServer
            .getCommandManager()
            .register(
                proxyServer
                    .getCommandManager()
                    .metaBuilder(
                        SettingsConfiguration.COMMAND_IGNORE_NAME.getString()
                    )
                    .aliases(
                        SettingsConfiguration.COMMAND_IGNORE_ALIASES.getStringList()
                            .toArray(new String[0])
                    )
                    .build(),
                new IgnoreCommand(this)
            );

        proxyServer
            .getCommandManager()
            .register(
                proxyServer
                    .getCommandManager()
                    .metaBuilder(
                        SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_NAME.getString()
                    )
                    .aliases(
                        SettingsConfiguration.COMMAND_TOGGLE_MESSAGES_ALIASES.getStringList()
                            .toArray(new String[0])
                    )
                    .build(),
                new ToggleMessagesCommand(this)
            );

        proxyServer
            .getCommandManager()
            .register(
                proxyServer
                    .getCommandManager()
                    .metaBuilder(
                        SettingsConfiguration.COMMAND_SOCIAL_SPY_NAME.getString()
                    )
                    .aliases(
                        SettingsConfiguration.COMMAND_SOCIAL_SPY_ALIASES.getStringList()
                            .toArray(new String[0])
                    )
                    .build(),
                new SocialSpyCommand(this)
            );
    }

    private void loadLibraries(boolean useMySql) {
        if (useMySql) {
            Library hikariCp = Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("4.0.3")
                .build();
            Library mysqlConnector = Library.builder()
                .groupId("mysql")
                .artifactId("mysql-connector-java")
                .version("8.0.19")
                .build();

            libraryManager.loadLibrary(hikariCp);
            libraryManager.loadLibrary(mysqlConnector);
        } else {
            Library sqLite = Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.36.0.3")
                .build();

            libraryManager.loadLibrary(sqLite);
        }
    }

    public Configuration getConfig() {
        return configManager.get("config.yml");
    }

    public Configuration getMessages() {
        return configManager.get("messages.yml");
    }
}
