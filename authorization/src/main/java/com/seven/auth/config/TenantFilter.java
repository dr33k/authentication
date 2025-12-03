package com.seven.auth.config;

import com.seven.auth.account.Account;
import com.seven.auth.account.AccountRepository;
import com.seven.auth.application.ApplicationRepository;
import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.util.Constants;
import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class TenantFilter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    //These URIs do not require a tenant id
    private final List<String> whitelist = List.of("/swagger", "/swagger-ui", "/v3/api-docs", Constants.PATH_PREFIX+"/applications");
    private final ApplicationRepository applicationRepository;
    private final EntityManager em;

    public TenantFilter(ApplicationRepository applicationRepository, EntityManager em) {
        this.applicationRepository = applicationRepository;
        this.em = em;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("---------------------------------------AUTHORIZATION---------------------------------------------------------------");
            setTenant(request, (String) request.getAttribute("tenant"));
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clearTenant();
            TenantContext.clearAuditor();
        }
    }

    private void setTenant(HttpServletRequest request, String tenant) {
        try {
            String path = request.getRequestURI();
            //Paths not visited by users
            if (isPathWhitelisted(path)) {
                log.info("WHITELISTED: {}", path);
                TenantContext.setCurrentTenant(Constants.PUBLIC_SCHEMA);
            }

            //Authentication paths
            else if (path.startsWith("/su/auth/")) {
                log.info("Superuser authentication path");
                TenantContext.setCurrentTenant(Constants.AUTHORIZATION_SCHEMA);
            } else if (path.startsWith("/auth/")) {
                log.info("Authentication path");
                String tenantId = request.getHeader("X-Tenant-Id");
                assert tenantId != null : "Tenant not provided";
                tenant = applicationRepository.findById(UUID.fromString(tenantId)).orElseThrow(() -> new ConflictException("Tenant with id %s not found".formatted(tenantId))).getSchemaName();
                TenantContext.setCurrentTenant(tenant);
            }

            //Regular paths where tenantIds are optional
            else if (Constants.AUTHORIZATION_SCHEMA.equals(tenant)) {
                String tenantId = request.getHeader("X-Tenant-Id");
                Account schemaAdmin = null;
                if (tenantId != null) {
                    tenant = applicationRepository.findById(UUID.fromString(tenantId)).orElseThrow(() -> new ConflictException("Tenant with id %s not found".formatted(tenantId))).getSchemaName();
                    schemaAdmin = (Account) em.createNativeQuery("SELECT * FROM \"%s\".auth_account WHERE email = '%s';".formatted(tenant, tenant+"@seven.com"), Account.class).getSingleResult();
                    log.info("SCHEMA ADMIN: {}", schemaAdmin);
                }
                TenantContext.setCurrentTenant(tenant);
                TenantContext.setAuditor(schemaAdmin);
            } else {
                assert tenant != null : "Tenant not provided";
                TenantContext.setCurrentTenant(tenant);
            }
            log.info("Routed to tenant: {}", TenantContext.getCurrentTenant());
        } catch (Exception e) {
            String msg = "Error routing to tenant %s : %s".formatted(tenant, e.getMessage());
            log.error(msg);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
        }
    }

    private boolean isPathWhitelisted(String path) {
        return whitelist.stream().anyMatch(path::startsWith);
    }
}
