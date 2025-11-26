package com.seven.auth.config.filter;

import com.seven.auth.application.ApplicationRepository;
import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.util.Constants;
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
public class TenantRouter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationRepository applicationRepository;

    private final List<String> whitelist = List.of("su");

    public TenantRouter(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader("X-Tenant-Id");
        try {
            log.info("Routing to tenant with id {}", tenantId);

            if(isPathWhitelisted(request.getContextPath())){
                TenantContext.setCurrentTenant(Constants.AUTHORIZATION_SCHEMA_NAME);
                doFilterInternal(request, response, filterChain);
            }

            //Process tenant
            assert tenantId != null: "Tenant id not provided";
            UUID tenantUUID = UUID.fromString(tenantId);

            //Set current tenant
            String schemaName = applicationRepository.findById(tenantUUID).orElseThrow(()->new ConflictException("Tenant with id %s not found".formatted(tenantId))).getSchemaName();
            TenantContext.setCurrentTenant(schemaName);

            log.info("Routed to tenant: {} ; id: {}", schemaName, tenantId);
        }catch (Exception e){
            log.error("Error routing to tenant with id {} :", tenantId, e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private boolean isPathWhitelisted(String path) {
        log.info("PATH: {}",path);
        String[] subpaths = path.split("/");
        return whitelist.contains(subpaths[0]);
    }
}
