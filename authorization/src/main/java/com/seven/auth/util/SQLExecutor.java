package com.seven.auth.util;

import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.domain.DomainDTO;
import com.seven.auth.permission.PermissionDTO;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The SQLExecutor is dedicated to running raw SQL using a JdbcTemplate or a provided DB Connection
 */
public class SQLExecutor {

    public static void insertDomains(Connection dataSourceConnection, ApplicationDTO.Create appRequest, Logger log) throws SQLException {
        try {
            log.info("Inserting Asset Types and related Permissions in Provisioned Schema");

            dataSourceConnection.setAutoCommit(false);
            List<DomainDTO.Create> domains = appRequest.domains();

            //Insert Domains
            try (PreparedStatement domainPS = dataSourceConnection.prepareStatement(String.format("SELECT \"%s\".insert_domain(?,?,?,?)", appRequest.schemaName()));
                 PreparedStatement permissionPS = dataSourceConnection.prepareStatement(String.format("SELECT \"%s\".insert_permission(?,?,?,?,?,?)", appRequest.schemaName()))) {
                for (DomainDTO.Create domain : domains) {
                    UUID domainId = UUID.randomUUID();
                    domainPS.setString(1, appRequest.schemaName());
                    domainPS.setObject(2, domainId);
                    domainPS.setString(3, domain.name());
                    domainPS.setString(4, domain.description());

                    domainPS.addBatch();

                    for (PermissionDTO.Create permission : domain.permissions()) {
                        permissionPS.setString(1, appRequest.schemaName());
                        permissionPS.setObject(2, UUID.randomUUID());
                        permissionPS.setString(3, permission.name());
                        permissionPS.setString(4, permission.type().name());
                        permissionPS.setObject(5, domainId);
                        permissionPS.setString(6, permission.description());

                        permissionPS.addBatch();
                    }
                }
                domainPS.executeBatch();
                permissionPS.executeBatch();
            }

            dataSourceConnection.commit();
            dataSourceConnection.setAutoCommit(true);
            log.info("Domain, Permissions remotely inserted successfully");
        } catch (Exception e) {
            dataSourceConnection.rollback();
            log.error("Exception inserting Asset Types. Message: {}", e.getMessage());
            throw e;
        }
    }
//
//    public static boolean checkAuthorizationStatus(JdbcTemplate jdbcTemplate, String provisionedSchemaName, DomainEntity domainEntity, String permissionName, String principalEmail, String assetId, Logger log) throws IOException {
//        log.info("Checking Authorization status\n" +
//                        "Provisioned schema name: {}\n" +
//                        "AccessConstraints view name: {}\n" +
//                        "Domain name: {}\n" +
//                        "Permission name: {}\n" +
//                        "Principal email: {}\n" +
//                        "Asset id: {}",
//                provisionedSchemaName,
//                domainEntity.getAccessConstraintsViewName(),
//                domainEntity.getName(),
//                permissionName,
//                principalEmail,
//                assetId);
//
//
//        String preparedStatementString = String.format("SELECT \"%s\".check_authorization_status(?,?,?,?,?,?)", provisionedSchemaName);
//        Boolean isAuthorized = jdbcTemplate.queryForObject(preparedStatementString, Boolean.class,
//                provisionedSchemaName,
//                domainEntity.getAccessConstraintsViewName(),
//                principalEmail,
//                permissionName,
//                domainEntity.getName(),
//                assetId
//        );
//        return Boolean.TRUE.equals(isAuthorized);
//    }

    public static boolean schemaExists(JdbcTemplate jdbcTemplate, String provisionedSchemaName, Logger log) throws SQLException {
        Boolean exists = jdbcTemplate.queryForObject("SELECT EXISTS( SELECT 1 FROM information_schema.schemata WHERE schema_name = ?) AS does_exist", Boolean.class, provisionedSchemaName);
        log.info("Schema: {} exists: {}", provisionedSchemaName, exists);
        return Boolean.TRUE.equals(exists);
    }

    private static String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty())
            return "";
        return "\"" + identifier.replaceAll("\\W", "") + "\"";
    }
}
