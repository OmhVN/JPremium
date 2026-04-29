package com.community.jpremium.storage;

import java.sql.Connection;

public interface ConnectionFactory {
    public void open();

    public void close();

    public Connection getConnection();
}

