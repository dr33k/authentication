package com.seven.auth.config.schema;

import com.seven.auth.config.threadlocal.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public void setTenantIdentifier(String tenantIdentifier) {
        log.debug("Setting Tenant Schema: {}", tenantIdentifier);
        TenantContext.setCurrentTenant(tenantIdentifier);
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}