package com.community.jpremium.storage.hikari;

import com.community.jpremium.storage.ConnectionFactory;
import com.community.jpremium.storage.StorageConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public abstract class AbstractHikariConnectionFactory
implements ConnectionFactory {
    private final StorageConfig storageConfig;
    private final HikariDataSource hikariDataSource = new HikariDataSource();

    public AbstractHikariConnectionFactory(StorageConfig storageConfig) {
        this.storageConfig = Objects.requireNonNull(storageConfig, "configuration");
    }

    protected abstract void configure(StorageConfig storageConfig, HikariConfig hikariConfig);

    @Override
    public void open() {
        this.hikariDataSource.setPoolName("JPremium-Hikari");
        this.hikariDataSource.setUsername(this.storageConfig.getUsername());
        this.hikariDataSource.setPassword(this.storageConfig.getPassword());
        this.hikariDataSource.setMinimumIdle(this.storageConfig.getMinimumIdle());
        this.hikariDataSource.setMaxLifetime((long)this.storageConfig.getMaxLifetimeMillis());
        this.hikariDataSource.setMaximumPoolSize(this.storageConfig.getMaximumPoolSize());
        this.hikariDataSource.setConnectionTimeout((long)this.storageConfig.getConnectionTimeoutMillis());
        this.hikariDataSource.setKeepaliveTime((long)this.storageConfig.getKeepaliveMillis());
        this.storageConfig.getProperties().forEach(this.hikariDataSource::addDataSourceProperty);
        this.configure(this.storageConfig, (HikariConfig)this.hikariDataSource);
    }

    @Override
    public void close() {
        this.hikariDataSource.close();
    }

    @Override
    public Connection getConnection() {
        try {
            return this.hikariDataSource.getConnection();
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }
}
