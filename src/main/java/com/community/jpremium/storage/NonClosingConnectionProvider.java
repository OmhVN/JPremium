package com.community.jpremium.storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class NonClosingConnectionProvider {
    private final Connection delegateConnection;
    private final Connection nonClosingConnection;

    public NonClosingConnectionProvider(Connection connection) {
        this.delegateConnection = connection;
        this.nonClosingConnection = (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class}, this::invokeProxyMethod);
    }

    private Object invokeProxyMethod(Object object, Method method, Object[] objectArray) throws Throwable {
        if ("close".equals(method.getName()) && method.getParameterCount() == 0) {
            return null;
        }
        try {
            return method.invoke(this.delegateConnection, objectArray);
        }
        catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }

    public Connection getConnection() {
        return this.nonClosingConnection;
    }

    public void closeUnderlyingConnection() {
        try {
            this.delegateConnection.close();
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }
}
