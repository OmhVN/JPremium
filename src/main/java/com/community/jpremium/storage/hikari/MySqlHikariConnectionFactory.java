package com.community.jpremium.storage.hikari;

import com.community.jpremium.storage.hikari.AbstractHikariConnectionFactory;
import com.community.jpremium.storage.StorageConfig;
import com.zaxxer.hikari.HikariConfig;

public class MySqlHikariConnectionFactory
extends AbstractHikariConnectionFactory {
    public MySqlHikariConnectionFactory(StorageConfig storageConfig) {
        super(storageConfig);
    }

    @Override
    protected void configure(StorageConfig storageConfig, HikariConfig hikariConfig) {
        hikariConfig.setDriverClassName("com.community.jpremium.library.com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s/%s", storageConfig.getHostAndPort(), storageConfig.getDatabase()));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
    }
}
