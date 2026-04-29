package com.community.jpremium.storage.sqlite;

import com.community.jpremium.common.runtime.RuntimeDependency;
import com.community.jpremium.common.runtime.RuntimeDependencyManager;
import com.community.jpremium.storage.sqlite.AbstractSqliteConnectionFactory;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class SqliteConnectionFactory
extends AbstractSqliteConnectionFactory {
    private static final Set<RuntimeDependency> SQLITE_DEPENDENCIES = Set.of(
            RuntimeDependency.SQLITE_JDBC,
            RuntimeDependency.SLF4J_API,
            RuntimeDependency.SLF4J_SIMPLE
    );

    private final RuntimeDependencyManager dependencyManager;

    public SqliteConnectionFactory(Path databasePath, RuntimeDependencyManager dependencyManager) {
        super(databasePath);
        this.dependencyManager = Objects.requireNonNull(dependencyManager, "dependencyManager");
    }

    @Override
    protected Connection openConnection(Path databasePath) {
        try {
            RuntimeDependencyManager.IsolatedUrlClassLoader classLoader = this.dependencyManager.getIsolatedClassLoader(SQLITE_DEPENDENCIES);
            Class<?> sqliteConnectionClass = classLoader.loadClass("org.sqlite.jdbc4.JDBC4Connection");
            Constructor<?> constructor = sqliteConnectionClass.getConstructor(String.class, String.class, Properties.class);
            return (Connection)constructor.newInstance(databasePath.toString(), databasePath.toString(), new Properties());
        } catch (Exception e) {
            throw new RuntimeException("Could not open SQLite connection: " + databasePath, e);
        }
    }
}
