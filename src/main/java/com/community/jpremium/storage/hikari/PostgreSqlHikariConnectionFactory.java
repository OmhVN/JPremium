package com.community.jpremium.storage.hikari;

import com.community.jpremium.storage.hikari.AbstractHikariConnectionFactory;
import com.community.jpremium.storage.StorageConfig;
import com.zaxxer.hikari.HikariConfig;

public class PostgreSqlHikariConnectionFactory
extends AbstractHikariConnectionFactory {
    public PostgreSqlHikariConnectionFactory(StorageConfig storageConfig) {
        super(storageConfig);
    }

    @Override
    protected void configure(StorageConfig storageConfig, HikariConfig hikariConfig) {
        hikariConfig.setDriverClassName("com.community.jpremium.library.org.postgresql.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:postgresql://%s/%s", storageConfig.getHostAndPort(), storageConfig.getDatabase()));
    }
}
