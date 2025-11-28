package com.seven.auth.config;

import com.seven.auth.config.threadlocal.TenantContext;
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

@Component
public class TenantFilter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final List<String> whitelist = List.of("/su/auth", "/swagger", "/swagger-ui", "/v3/api-docs", "/applications");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            setTenant(request.getRequestURI(), (String) request.getAttribute("tenant"));
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clearTenant();
        }
    }

    private void setTenant(String path, String tenant){
        try {
            if (isPathWhitelisted(path)) {
                TenantContext.setCurrentTenant(Constants.AUTHORIZATION_SCHEMA_NAME);
                log.info("WHITELISTED: {}", path);
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
