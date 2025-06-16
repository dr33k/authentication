package com.seven.auth.config.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider {

    @Autowired
    private DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schemaName) throws SQLException {
        final Connection connection = dataSource.getConnection();
        connection.createStatement().execute(String.format("SET SCHEMA '%s';", schemaName));

        //Get DB vendor
        TenantContext.setCurrentDbVendor(connection.getMetaData().getDatabaseProductName());

        return connection;
    }

    @Override
    public void releaseConnection(String schemaName, Connection connection) throws SQLException {
        connection.createStatement().execute("SET SCHEMA 'public';");
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
