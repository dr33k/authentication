package com.seven.auth.config.schema;

import com.seven.auth.config.threadlocal.TenantContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.connections.spi.DatabaseConnectionInfo;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SchemaTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

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
    public DatabaseConnectionInfo getDatabaseConnectionInfo(Dialect dialect) {
        return MultiTenantConnectionProvider.super.getDatabaseConnectionInfo(dialect);
    }

    @Override
    public Connection getConnection(String schemaName) throws SQLException {
        final Connection connection = dataSource.getConnection();
        connection.createStatement().execute("SET SCHEMA '%s';".formatted(schemaName));
        TenantContext.setCurrentDbVendor(connection.getMetaData().getDatabaseProductName().toLowerCase());
        return connection;
    }

    @Override
    public void releaseConnection(String schemaName, Connection connection) throws SQLException {
        connection.createStatement().execute("SET SCHEMA 'public';");
        TenantContext.clearCurrentDbVendor();
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
        if (unwrapType.isAssignableFrom(getClass())) {
            return unwrapType.cast(this);
        }
        return null;
    }
}
