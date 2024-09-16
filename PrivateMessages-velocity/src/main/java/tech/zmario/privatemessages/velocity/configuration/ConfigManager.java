package tech.zmario.privatemessages.velocity.configuration;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import tech.zmario.privatemessages.common.configuration.Configuration;
import tech.zmario.privatemessages.common.configuration.ConfigurationProvider;
import tech.zmario.privatemessages.common.configuration.YamlConfiguration;
import tech.zmario.privatemessages.velocity.PrivateMessagesVelocity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ConfigManager {

    private final HashMap<String, Configuration> configs;
    private File folder;
    private File pluginJar;

    public ConfigManager(PrivateMessagesVelocity plugin, PluginDescription pluginDescription) {
        this.configs = new HashMap<>();
        try {

            File serverJar = new File(Plugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            pluginJar = new File(serverJar.getParentFile(), pluginDescription.getSource().get().toString());
            folder = new File(serverJar.getParentFile() + "/plugins/" + pluginDescription.getId());

            if (!folder.exists()) {
                folder.mkdir();
            }

            plugin.setPluginFolder(folder);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();

        }
    }

    public Configuration get(String file) {
        return this.configs.getOrDefault(file, null);
    }

    public void create(String file, String source) {
        File resourcePath = new File(folder + "/" + file);

        if (!resourcePath.exists()) {
            createYAML(file, source, false);
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
            createYAML(file, false);
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

    private void createYAML(String resourcePath, String source, boolean replace) {
        try {
            File file = new File(folder + "/" + resourcePath);

            if (!file.getParentFile().exists() || !file.exists()) {

                file.getParentFile().mkdir();

                if (!file.exists()) {
                    file.createNewFile();
                }

                boolean forcereplace = replace;

                if (file.length() == 0) {
                    forcereplace = true;
                }

                if (forcereplace) {
                    Files.copy(getResourceAsStream(source),
                            file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else Files.copy(getResourceAsStream(source), file.toPath());
            }
        } catch (IOException | ClassNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void createYAML(String resourcePath, boolean replace) {
        this.createYAML(resourcePath, resourcePath, replace);
    }


    private InputStream getResourceAsStream(String name) throws ClassNotFoundException, URISyntaxException, IOException {
        ZipFile file = new ZipFile(pluginJar);
        ZipInputStream zip = new ZipInputStream(pluginJar.toURL().openStream());

        boolean stop = false;

        while (!stop) {
            ZipEntry e = zip.getNextEntry();

            if (e == null) {
                stop = true;
            } else if (e.getName().equals(name)) {
                return file.getInputStream(e);
            }

        }
        return null;
    }
}