package com.community.jpremium.storage;

import java.util.Map;

public class StorageConfig {
    private String hostAndPort;
    private String username;
    private String password;
    private String database;
    private int maximumPoolSize;
    private int maxLifetimeMillis;
    private int minimumIdle;
    private int keepaliveMillis;
    private int connectionTimeoutMillis;
    private Map<String, String> properties;

    public String getHostAndPort() {
        return this.hostAndPort;
    }

    public void setHostAndPort(String text) {
        this.hostAndPort = text;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String text) {
        this.username = text;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String text) {
        this.password = text;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setDatabase(String text) {
        this.database = text;
    }

    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    public void setMaximumPoolSize(int n) {
        this.maximumPoolSize = n;
    }

    public int getMaxLifetimeMillis() {
        return this.maxLifetimeMillis;
    }

    public void setMaxLifetimeMillis(int n) {
        this.maxLifetimeMillis = n;
    }

    public int getMinimumIdle() {
        return this.minimumIdle;
    }

    public void setMinimumIdle(int n) {
        this.minimumIdle = n;
    }

    public int getKeepaliveMillis() {
        return this.keepaliveMillis;
    }

    public void setKeepaliveMillis(int n) {
        this.keepaliveMillis = n;
    }

    public int getConnectionTimeoutMillis() {
        return this.connectionTimeoutMillis;
    }

    public void setConnectionTimeoutMillis(int n) {
        this.connectionTimeoutMillis = n;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> map) {
        this.properties = map;
    }
}

