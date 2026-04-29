package com.community.jpremium.common.config;

import com.community.jpremium.bungee.JPremium;
import java.util.List;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfigService {
    private static final ConfigurationProvider YAML_PROVIDER = ConfigurationProvider.getProvider(YamlConfiguration.class);
    private final JPremium plugin;
    private final String fileName;
    private Configuration configuration;

    public BungeeConfigService(JPremium jPremium, String text) {
        this.plugin = jPremium;
        this.fileName = text;
    }

    public int getInt(String text) {
        return this.configuration.getInt(text);
    }

    public boolean getBoolean(String text) {
        return this.configuration.getBoolean(text);
    }

    public String getString(String text) {
        return this.configuration.getString(text, null);
    }

    public List<String> getStringList(String text) {
        return this.configuration.getStringList(text);
    }

    public <T extends Enum<T>> T getEnum(Class<T> clazz, String text) {
        return Enum.valueOf(clazz, this.configuration.getString(text));
    }

    public <T extends Enum<T>> T getEnumOrDefault(Class<T> clazz, String text, T t) {
        return Enum.valueOf(clazz, this.configuration.getString(text, t.name()));
    }

    public void reload() {
        try {
            this.configuration = YAML_PROVIDER.load(this.plugin.ensureResourceFile(this.fileName).toFile());
        }
        catch (Exception exception) {
            throw new IllegalStateException("Could not load configuration file: " + this.fileName, exception);
        }
    }
}
