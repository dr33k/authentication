package com.seven.auth.config.tenant;

import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final ThreadLocal<ApplicationDTO> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_DB_VENDOR = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);


    public static ApplicationDTO getCurrentTenant() {
        ApplicationDTO applicationDTO = CURRENT_TENANT.get();
        return applicationDTO == null ? Constants.AUTHORIZATION_APPLICATION : applicationDTO;
    }

    public static void setCurrentTenant(ApplicationDTO tenant) {
        if (tenant == null) tenant = Constants.AUTHORIZATION_APPLICATION;
        log.info("TenantContext: setting current tenant: {}", tenant.schemaName());
        CURRENT_TENANT.set(tenant);
    }

    public static String getCurrentDbVendor() {
        String dbVendor = CURRENT_DB_VENDOR.get();
        return dbVendor == null ? "postgresql" : dbVendor.toLowerCase();
    }

    public static void setCurrentDbVendor(String currentDbVendor) {
        CURRENT_DB_VENDOR.set(currentDbVendor);
    }
}
