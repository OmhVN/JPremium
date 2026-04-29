package com.community.jpremium.common.config;

import com.community.jpremium.velocity.JPremiumVelocity;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class VelocityConfigService {
    private final JPremiumVelocity plugin;
    private final String fileName;
    private ConfigurationNode configurationNode;

    public VelocityConfigService(JPremiumVelocity jPremiumVelocity, String text) {
        this.plugin = jPremiumVelocity;
        this.fileName = text;
    }

    public int getInt(String text) {
        return this.configurationNode.node(new Object[]{text}).getInt();
    }

    public boolean getBoolean(String text) {
        return this.configurationNode.node(new Object[]{text}).getBoolean();
    }

    public String getString(String text) {
        return this.configurationNode.node(new Object[]{text}).getString();
    }

    public List<String> getStringList(String text) {
        ConfigurationNode configurationNode = this.configurationNode.node(new Object[]{text});
        if (configurationNode.isList() && !configurationNode.empty()) {
            try {
                return new ArrayList<String>(configurationNode.getList(String.class));
            }
            catch (SerializationException serializationException) {
                throw new RuntimeException(serializationException);
            }
        }
        return new ArrayList<String>();
    }

    public <T extends Enum<T>> T getEnum(Class<T> clazz, String text) {
        return Enum.valueOf(clazz, this.getString(text));
    }

    public <T extends Enum<T>> T getEnumOrDefault(Class<T> clazz, String text, T t) {
        return Enum.valueOf(clazz, this.configurationNode.node(new Object[]{text}).getString(t.name()));
    }

    public void reload() {
        try {
            this.configurationNode = ((YamlConfigurationLoader.Builder)YamlConfigurationLoader.builder().path(this.plugin.ensureResourceFile(this.fileName))).build().load();
        }
        catch (Exception exception) {
            throw new IllegalStateException("Could not load configuration file: " + this.fileName, exception);
        }
    }
}
