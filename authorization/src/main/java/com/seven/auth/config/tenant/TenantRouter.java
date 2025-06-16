package com.seven.auth.config.tenant;

import com.seven.auth.application.ApplicationRepository;
import io.micrometer.common.util.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TenantRouter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TenantRouter.class);
    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    public TenantRouter(ApplicationRepository applicationRepository, ModelMapper modelMapper) {
        this.applicationRepository = applicationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        log.info("Routing...");
        String tenantId = requestContext.getHeaders().getFirst("X-Tenant-Id");
        try {
            String method = requestContext.getRequest().getMethod().toLowerCase();
            String path = requestContext.getUriInfo().getPath();

            //If no tenantId was provided
            if (StringUtils.isBlank(tenantId)) {
                if (!isRequestAllowed(method, path)) {
                    requestContext.abortWith(
                            Response.status(Response.Status.FORBIDDEN)
                                    .entity("No Tenant Provided")
                                    .build()
                    );
                }
                TenantContext.setCurrentTenant(Constants.AUTHSHIELD_METADATA);
            } else if (isApplicationRequest(path)) {
                TenantContext.setCurrentTenant(Constants.AUTHSHIELD_METADATA);
            } else {
                ApplicationEntity applicationEntity = applicationRepository.findById(tenantId).orElse(null);

                //Abort requests with invalid tenantIds
                if (applicationEntity == null) {
                    requestContext.abortWith(
                            Response.status(Response.Status.FORBIDDEN)
                                    .entity("Invalid Tenant Provided")
                                    .build()
                    );
                    return;
                }
                TenantContext.setCurrentTenant(modelMapper.map(applicationEntity, Application.class));
            }
        } catch (Exception e) {
            log.error("Problem routing to specified tenant {}. Trace: ", tenantId, e);
            requestContext.abortWith(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(String.format("Problem routing to specified tenant %s", tenantId))
                            .build()
            );
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
