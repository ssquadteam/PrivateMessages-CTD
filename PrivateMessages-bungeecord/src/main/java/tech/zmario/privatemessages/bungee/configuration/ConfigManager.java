package tech.zmario.privatemessages.bungee.configuration;

import net.md_5.bungee.api.plugin.Plugin;
import tech.zmario.privatemessages.common.configuration.Configuration;
import tech.zmario.privatemessages.common.configuration.ConfigurationProvider;
import tech.zmario.privatemessages.common.configuration.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class ConfigManager {

    private final HashMap<String, Configuration> configs;
    private final Plugin plugin;
    private final File folder;

    public ConfigManager(Plugin plugin) {

        this.plugin = plugin;
        this.configs = new HashMap<>();

        folder = this.plugin.getDataFolder();

        if (!folder.exists()) {
            folder.mkdir();
        }

    }

    public Configuration get(String file) {
        return this.configs.getOrDefault(file, null);
    }

    public void create(String file, String source) {
        File resourcePath = new File(folder + "/" + file);

        if (!resourcePath.exists()) {
            createYAML(file, source);
        }

        reload(file);
    }

    public void create(String file) {
        create(file, file);
    }

    public void save(String file) {
        Configuration config = get(file);

        if (config == null) {
            throw new IllegalArgumentException("The specified configuration file doesn't exist!");
        }

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .save(config, new File(folder + "/" + file));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        this.put(file, config);
    }

    public void reload(String file) {

        if (!this.configs.containsKey(file)) {
            createYAML(file);
        }

        Configuration conf = this.load(file);

        this.put(file, conf);
    }

    public void reloadAll() {
        this.configs.keySet().forEach(this::reload);
    }

    private Configuration load(String file) {
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new File(folder + "/" + file));
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void put(String file, Configuration config) {
        this.configs.put(file, config);
    }

    private void createYAML(String resourcePath, String source) {
        try {
            File file = new File(folder + "/" + resourcePath);

            if (!file.getParentFile().exists() || !file.exists()) {

                file.getParentFile().mkdir();

                if (!file.exists()) {
                    file.createNewFile();
                }

                Files.copy(plugin.getResourceAsStream(source),
                        file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createYAML(String resourcePath) {
        this.createYAML(resourcePath, resourcePath);
    }
}