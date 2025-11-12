package com.seven.auth.config.threadlocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_DB_VENDOR = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);


    public static String getCurrentTenant(){
        String currentTenant = CURRENT_TENANT.get();
        return currentTenant == null ?"public" : currentTenant;
    }

    public static void setCurrentTenant(String schemaName){
        log.info("TenantContext: setting current tenant schema {}", schemaName);
        CURRENT_TENANT.set(schemaName == null ? "public" : schemaName);
    }

    public static String getCurrentDbVendor() {
        String dbVendor = CURRENT_DB_VENDOR.get();
        return dbVendor == null ? "postgres" : dbVendor.toLowerCase();
    }

    public static void setCurrentDbVendor(String currentDbVendor) {
        CURRENT_DB_VENDOR.set(currentDbVendor);
    }
}
