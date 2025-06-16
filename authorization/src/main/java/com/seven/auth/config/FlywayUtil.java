package com.seven.auth.config;

import com.hcb.services.authshield.accessconstraints.AccessConstraints;
import com.hcb.services.authshield.pojos.ServiceMetadata;
import com.hcb.services.authshield.tenancy.TenantContext;
import com.hcb.services.authshield.util.Constants;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class FlywayUtil {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public Flyway buildTenantFlyway(DataSource dataSource, AccessConstraints accessConstraints) {
        return buildTenantFlyway(dataSource, accessConstraints.getDbName(), accessConstraints.getSchemaName());
    }

    public Flyway buildTenantFlyway(DataSource dataSource, ServiceMetadata serviceMetadata) {
        return buildTenantFlyway(dataSource, serviceMetadata.getDbName(), serviceMetadata.getSchemaName());
    }

    private Flyway buildTenantFlyway(DataSource dataSource, String dbName, String schemaName){
        log.info("Instantiating flyway for Schema: {} in DB: {}", schemaName, dbName);
        String dbVendor = TenantContext.getCurrentDbVendor();
        log.info("DB Vendor: {}", dbVendor);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas(schemaName);
        flyway.setLocations(String.format(Constants.TENANT_MIGRATION_SCRIPTS_PATH, dbVendor));
        log.info("Instantiated flyway for Schema: {} in DB: {}", schemaName, dbName);
        return flyway;
    }
}
