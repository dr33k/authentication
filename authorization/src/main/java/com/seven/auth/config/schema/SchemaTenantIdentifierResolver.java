package com.seven.auth.config.schema;

import com.seven.auth.config.threadlocal.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private final Logger log = LoggerFactory.getLogger(getClass());


    public void setTenantIdentifier(String tenantIdentifier) {
        log.debug("Setting Tenant Schema: {}", tenantIdentifier);
        TenantContext.setCurrentTenant(tenantIdentifier);
    }

    public String getTenantIdentifier() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        // Return the identifier from the ThreadLocal, or a default
        String tenantId = getTenantIdentifier();
        if (tenantId == null) {
            // Handle cases where no tenant is set
            return "public";
        }
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}