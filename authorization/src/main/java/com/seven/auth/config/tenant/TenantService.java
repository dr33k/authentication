package com.seven.auth.config.tenant;

import com.hcb.services.authshield.accessconstraints.AccessConstraints;
import com.hcb.services.authshield.config.FlywayUtils;
import com.hcb.services.authshield.pojos.AssetType;
import com.hcb.services.authshield.util.EntityGenerator;
import com.hcb.services.authshield.util.SQLExecutor;
import com.hcb.services.authshield.util.Validator;
import com.seven.auth.application.Application;
import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.application.ApplicationRepository;
import com.seven.auth.config.FlywayUtil;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.ConflictException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The TenantService performs the singular task of registering a service and
 * populating a schema for it with tables using Flyway
 */
@Service
public class TenantService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationRepository applicationRepository;
    private final FlywayUtil flywayUtil;
    private final TenantConnectionProvider tenantConnectionProvider;
    private final EntityManager entityManager;
    private final ModelMapper modelMapper;

    @Value("${spring.datasource.username}")
    private String localDataSourceUsername;
    @Value("${spring.datasource.password}")
    private String localDataSourcePassword;
    @Value("${spring.datasource.driver-class-name}")
    private String localDataSourceDriverClassName;

    public TenantService(ApplicationRepository applicationRepository,
                         FlywayUtil flywayUtil,
                         EntityManager entityManager,
                         ModelMapper modelMapper) {
        this.applicationRepository = applicationRepository;
        this.flywayUtil = flywayUtil;
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
    }

    private DataSource createDataSource(String dbUrl) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username(localDataSourceUsername);
        dataSourceBuilder.password(localDataSourcePassword);
        dataSourceBuilder.driverClassName(localDataSourceDriverClassName);
        dataSourceBuilder.url(dbUrl);
        return dataSourceBuilder.build();
    }

    public ApplicationDTO register(AccessConstraints accessConstraints) throws AuthorizationException {
        try {
            String serviceName = accessConstraints.getServiceName();
            String orgId = accessConstraints.getOrgId();

            log.info("Registering new service: {}", serviceName);

            //Check if service name and orgId was provided
            if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(orgId)) {
                log.info("Service name or org id is missing");
                throw new ConflictException("Service name or org id is missing");
            }

            //Start at default schema, check if a service by this name already exists
            Application application = applicationRepository.findByServiceName(serviceName).orElse(null);
            if (application == null) {
                application = provisionSchema(accessConstraints);
            }

            //Return service schema
            ApplicationDTO applicationDTO = modelMapper.map(application, ApplicationDTO.class);
            log.info("Service: {} registered successfully with tenantId (serviceSchemaId): {}", serviceMetadata.getServiceName(), serviceMetadata.getId());

            return applicationDTO;
        } catch (AuthorizationException e) {
            log.error("Authshield exception registering service {}; Message: {}", accessConstraints.getServiceName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering service {}; Trace: {}", accessConstraints.getServiceName(), e);
            throw new ClientException(e.getMessage());
        }
    }

    private Application provisionSchema(AccessConstraints accessConstraints) throws AuthorizationException {
        try {
            log.info("Provisioning new schema for service: {}", accessConstraints.getServiceName());

            List<AssetType> assetTypes = accessConstraints.getAssetTypes();

            //Validate format of AssetType AccessConstraints provided
            Validator.validateAssetTypesFormat(assetTypes, log);

            // Commence operations to Remote DB, provision authshield managed schema and asset types there
            pingRemoteDB(accessConstraints);

            Application application = EntityGenerator.generateApplication(accessConstraints);
            applicationRepository.saveAndFlush(application);

            log.info("Provisioned new schema: {}", accessConstraints.getSchemaName());
            return application;
        } catch (NullPointerException e) {
            e.printStackTrace();
            log.error("Null Pointer Exception encountered while provisioning schema . Request: {}", accessConstraints);
            throw new ClientException(String.format("Null value encountered while provisioning schema; Request: %s", accessConstraints));
        } catch (URISyntaxException e) {
            log.error("URISyntaxException provisioning schema. DB URL: {} . Trace: {}", accessConstraints.getDbUrl(), e);
            throw new ClientException(String.format("Incorrectly formatted DB URL: %s", accessConstraints.getDbUrl()));
        } catch (SQLException e) {
            log.error("SQLException trying to provision schema. Message: {}", e.getMessage());
            throw new ClientException("Unable to provision schema. Make sure the database specified exists, is running and that all table, schema and column names specified exist in the database");
        } catch (AuthorizationException e) {
            log.error("AuthshieldException trying to provision schema. Message: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error trying to provision schema. Message: {}", e.getMessage());
            throw new ClientException(String.format("Error provisioning schema. Message: %s", e.getMessage()));
        }
    }

    private void pingRemoteDB(AccessConstraints accessConstraints) throws URISyntaxException, IOException, SQLException, AuthorizationException {
        String dbUrl = accessConstraints.getDbUrl();
        try {
            log.info("Commencing Operations on Remote DB remote DB: {}", dbUrl);

            //Validate DB Url
            Validator.isDbUrlValid(dbUrl, log);

            //Creates DataSource object
            log.info("Creating DataSource Object");
            DataSource remoteDataSource = createDataSource(dbUrl);

            //Create flyway instance for schema-to-be-created
            Flyway tenantFlyway = flywayUtil.buildTenantFlyway(remoteDataSource, accessConstraints);

            //Clean up any incompletely provisioned schemas with the same name from the db
            tenantFlyway.clean();

            //Create and migrate new schema
            tenantFlyway.migrate();
            log.info("Authshield managed schema: {} created successfully in DB: {}", accessConstraints.getSchemaName(), accessConstraints.getDbName());

            //Open a connection to the DB and begin the Access Constraints provisioning process
            try (Connection remoteDbConnection = remoteDataSource.getConnection()) {
                log.info("Connected to Remote DB");
                provisionAccessConstraintsViews(remoteDbConnection, accessConstraints);

                //Insert AssetTypes and related Permissions in DB
                SQLExecutor.insertAssetTypes(remoteDbConnection, accessConstraints, log);
                log.info("Closing connection to Remote DB");
            }

            log.info("Finished Operations on Remote DB. Datasource: {} registered in memory", accessConstraints.getDbName());
        } catch (URISyntaxException e) {
            routingDataSource.unregisterDataSource(accessConstraints.getDbName());
            log.error("URISyntaxException while trying to save asset types on remote DB: {}. Message: {}", dbUrl, e.getMessage());
            throw e;
        } catch (SQLException e) {
            routingDataSource.unregisterDataSource(accessConstraints.getDbName());
            log.error("SQLException; Error trying to save asset types on remote DB: {}. Message: {}", dbUrl, e.getMessage());
            throw e;
        } catch (Exception e) {
            routingDataSource.unregisterDataSource(accessConstraints.getDbName());
            log.error("Error while trying to save asset types on remote DB: {}. Message: {}", dbUrl, e.getMessage());
            throw e;
        }
    }

    public void provisionAccessConstraintsViews(Connection connection, AccessConstraints accessConstraints) throws SQLException, IOException {
        log.info("Provisioning Access Constraints Views");
        //Validate all table names and column names provided in request. No exception means everything is in order
        SQLExecutor.checkColumnsExist(connection, accessConstraints.getAssetTypes(), log);

        //Create AccessConstraints View in DB
        SQLExecutor.createAccessConstraintsViews(connection, accessConstraints, log);
        log.info("Access Constraints Views provisioned on DB: {}, Schema: {}", accessConstraints.getDbName(), accessConstraints.getSchemaName());
    }

    public void dropSchema(ApplicationDTO serviceMetadata) {
        try {
            log.info("Dropping schema: {} in DB: {}", serviceMetadata.getSchemaName(), serviceMetadata.getDbName());

            DataSource remoteDataSource = (DataSource) routingDataSource.getDataSources().get(serviceMetadata.getDbName());
            if (remoteDataSource == null) {
                String message = String.format("DataSource for %s is not registered or may have previously been dropped", serviceMetadata.getServiceName());
                log.warn(message);
                return;
            }
            Flyway tenantFlyway = flywayUtil.buildTenantFlyway(remoteDataSource, serviceMetadata);
            tenantFlyway.clean();
            routingDataSource.unregisterDataSource(serviceMetadata.getDbName());

            log.info("Schema: {} (if exists) dropped successfully", serviceMetadata.getSchemaName());
        } catch (Exception e) {
            log.error("Error dropping schema {}. Trace: {}", serviceMetadata.getSchemaName(), e);
            throw e;
        }
    }
}
