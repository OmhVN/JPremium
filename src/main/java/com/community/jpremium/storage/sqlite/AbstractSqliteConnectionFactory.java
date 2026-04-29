package com.community.jpremium.storage.sqlite;

import com.community.jpremium.storage.ConnectionFactory;
import com.community.jpremium.storage.NonClosingConnectionProvider;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Objects;

public abstract class AbstractSqliteConnectionFactory
implements ConnectionFactory {
    private final Path databasePath;
    private NonClosingConnectionProvider connectionHolder;

    public AbstractSqliteConnectionFactory(Path databasePath) {
        this.databasePath = Objects.requireNonNull(databasePath, "databasePath");
    }

    protected abstract Connection openConnection(Path databasePath);

    @Override
    public void open() {
        this.connectionHolder = new NonClosingConnectionProvider(this.openConnection(this.databasePath));
    }

    @Override
    public void close() {
        this.connectionHolder.closeUnderlyingConnection();
    }

    @Override
    public Connection getConnection() {
        return this.connectionHolder.getConnection();
    }
}
