package com.seven.auth.application;

import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.domain.DomainDTO;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.util.Constants;
import com.seven.auth.util.SQLExecutor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * The TenantService performs the singular task of registering an app and
 * populating a schema for it with tables using Flyway
 */
@Service
public class TenantService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;
    private final DataSource dataSource;

    public TenantService(ApplicationRepository applicationRepository,
                         ModelMapper modelMapper,
                         DataSource dataSource) {
        this.applicationRepository = applicationRepository;
        this.modelMapper = modelMapper;
        this.dataSource = dataSource;
    }

    public ApplicationDTO.Record register(ApplicationDTO.Create appRequest) throws AuthorizationException {
        try {
            String appName = appRequest.name();
            log.info("Registering new app: {}", appName);

            //Check if app by this name already exists
            Application application = applicationRepository.findByName(appName).orElse(null);
            if (application == null) {
                application = provisionSchema(appRequest);
            }

            ApplicationDTO.Record appRecord = modelMapper.map(application, ApplicationDTO.Record.class);
            log.info("App: {} registered successfully with id: {}", appRecord.name(), appRecord.id());

            return appRecord;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering app {}; Message: {}", appRequest.name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering app {}; Trace:", appRequest.name(), e);
            throw new ClientException(e.getMessage());
        }
    }

    private Application provisionSchema(ApplicationDTO.Create appRequest) throws AuthorizationException {
        try {
            log.info("Provisioning new schema for app: {}", appRequest.name());

            List<DomainDTO.Create> domains = appRequest.domains();

            //Create flyway instance for schema-to-be-created
            Flyway tenantFlyway = buildTenantFlyway(dataSource, appRequest);

            //Clean up any incompletely provisioned schemas with the same name from the db
            tenantFlyway.clean();

            //Create and migrate new schema
            tenantFlyway.migrate();
            log.info("Schema: {} created successfully in DB", appRequest.schemaName());

            //Open a connection to the DB and begin the population process
            try (Connection remoteDbConnection = dataSource.getConnection()) {
                //Insert Domains and related Permissions in DB
                SQLExecutor.insertDomains(remoteDbConnection, appRequest, log);
            }
            Application application = modelMapper.map(appRequest, Application.class);
            applicationRepository.saveAndFlush(application);

            log.info("Provisioned new schema: {}", appRequest.name());
            return application;
        } catch (Exception e) {
            log.error("Error trying to provision schema. Trace:", e);
            throw new ClientException(String.format("Error provisioning schema. Message: %s", e.getMessage()));
        }
    }

    /**
     * This runs the script responsible for dropping an application's schema
     * @param app
     * @throws IOException
     */
    public void dropSchema(Application app){
        try {
            log.info("Dropping schema: {} in DB", app.getName());

            Flyway tenantFlyway = buildTenantFlyway(dataSource, app);
            tenantFlyway.clean();

            log.info("Schema: {} (if exists) dropped successfully", app.getName());
        } catch (Exception e) {
            log.error("Error dropping schema {}. Trace:", app.getName(), e);
            throw e;
        }
    }

    public Flyway buildTenantFlyway(DataSource dataSource, Application app) {
        return buildTenantFlyway(dataSource, app.getSchemaName());
    }
    public Flyway buildTenantFlyway(DataSource dataSource, ApplicationDTO.Create appRequest) {
        return buildTenantFlyway(dataSource, appRequest.schemaName());
    }
    public Flyway buildTenantFlyway(DataSource dataSource,String schemaName) {
        log.info("Instantiating flyway for Schema: {} in DB", schemaName);
        String dbVendor = TenantContext.getCurrentDbVendor();
        log.info("DB Vendor: {}", dbVendor);

        ClassicConfiguration flywayConfig = new ClassicConfiguration();
        flywayConfig.setDataSource(dataSource);
        flywayConfig.setSchemas(new String[]{schemaName});
        flywayConfig.setLocations(new Location(String.format(Constants.TENANT_MIGRATION_SCRIPTTS_PATH, dbVendor)));
        log.info("Instantiated flyway for Schema: {} in DB", schemaName);
        return new Flyway(flywayConfig);
    }
}