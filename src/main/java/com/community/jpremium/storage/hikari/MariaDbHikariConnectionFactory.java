package com.community.jpremium.storage.hikari;

import com.community.jpremium.storage.hikari.AbstractHikariConnectionFactory;
import com.community.jpremium.storage.StorageConfig;
import com.zaxxer.hikari.HikariConfig;

public class MariaDbHikariConnectionFactory
extends AbstractHikariConnectionFactory {
    public MariaDbHikariConnectionFactory(StorageConfig storageConfig) {
        super(storageConfig);
    }

    @Override
    protected void configure(StorageConfig storageConfig, HikariConfig hikariConfig) {
        hikariConfig.setDriverClassName("com.community.jpremium.library.org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s/%s", storageConfig.getHostAndPort(), storageConfig.getDatabase()));
    }
}
