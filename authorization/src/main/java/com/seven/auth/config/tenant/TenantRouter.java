package com.seven.auth.config.tenant;

import com.seven.auth.application.Application;
import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.application.ApplicationRepository;
import com.seven.auth.util.Constants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantRouter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(TenantRouter.class);
    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    public TenantRouter(ApplicationRepository applicationRepository, ModelMapper modelMapper) {
        this.applicationRepository = applicationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("Routing...");
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        UUID tenantId;
        try {
            tenantId = UUID.fromString(httpServletRequest.getHeader("X-Tenant-Id"));
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Tenant Id Provided");
        }
        try {
            String method = httpServletRequest.getMethod().toLowerCase();
            String path = httpServletRequest.getContextPath();

            if (!isRequestAllowed(method, path)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No Tenant Provided");
            } else if (isApplicationRequest(path)) {
                TenantContext.setCurrentTenant(Constants.AUTHORIZATION_APPLICATION);
            } else {
                Application application = applicationRepository.findById(tenantId).orElse(null);

                //Abort requests with invalid tenantIds
                if (application == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Tenant Provided");
                }
                TenantContext.setCurrentTenant(modelMapper.map(application, ApplicationDTO.class));
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            log.error("Problem routing to specified tenant {}. Trace: ", tenantId, e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Problem routing to specified tenant");
        }
    }

    private static boolean isApplicationRequest(String path) {
        return path.startsWith("application");
    }

    private boolean isRequestAllowed(String method, String path) {
        return
                (method.equals("post") && path.equals("application"))
                        ||
                        (method.equals("get") && path.equals("openapi.json"));
    }
}
