package com.seven.auth.config.schema;

import org.hibernate.cfg.DefaultNamingStrategy;
import org.hibernate.cfg.Environment;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.hcb.services",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class SchemaTenantConfiguration {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                       DataSource dataSource,
                                                                       JpaProperties jpaProperties,
                                                                       SchemaTenantIdentifierResolver schemaTenantIdentifierResolver,
                                                                       SchemaTenantConnectionProvider schemaTenantConnectionProvider
    ){
        Map<String, Object> jpaPropertiesMap = new HashMap<>(jpaProperties.getProperties());
//        jpaPropertiesMap.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        jpaPropertiesMap.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, schemaTenantConnectionProvider);
        jpaPropertiesMap.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, schemaTenantIdentifierResolver);
        jpaPropertiesMap.put(Environment.PHYSICAL_NAMING_STRATEGY, new DefaultNamingStrategy());
        return builder
                .dataSource(dataSource)
                .packages("com.seven.auth")
                .properties(jpaPropertiesMap)
                .build();
    }
}
