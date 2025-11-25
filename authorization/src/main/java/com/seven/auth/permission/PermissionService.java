package com.seven.auth.permission;

import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.exception.NotFoundException;
import com.seven.auth.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Page<PermissionDTO.Record> getAll(Pagination pagination, PermissionDTO.Filter filter) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Permissionsfor Tenant: {}", tenant);
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<PermissionDTO.Record> permissionsResponse = permissionRepository
                    .findAll(PermissionSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(PermissionDTO.Record::from);

            log.info("Permissions retrieved successfully");
            return permissionsResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Permissions for Tenant: {} in service layer. Trace:", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public PermissionDTO.Record get(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Permission: {} for Tenant: {}", id, tenant);
        try {
            Permission permissionEntity = permissionRepository.findById(id).orElseThrow(() -> {
                log.error("Permission: {} not found", id);
                return new NotFoundException(String.format("Permission: %s not found", id));
            });

            PermissionDTO.Record response = PermissionDTO.Record.from(permissionEntity);
            log.info("Permission retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving permission in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Permission: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Deleting Permission: {} for Tenant: {}", id, tenant);
        try {
            permissionRepository.deleteById(id);
            log.info("Permission deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Permission {} not found.", id);
            throw new NotFoundException(String.format("Permission %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Permission: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public PermissionDTO.Record create(PermissionDTO.Create request) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Registering Permission for Tenant: {}", tenant);
        try {
            //Validate unique name for an organisation
            if (permissionRepository.existsByName(request.name())) {
                log.error("Permission with name '{}' already exists", request.name());
                throw new ConflictException(String.format("Permission with name '%s' already exists", request.name()));
            }

            Permission permissionEntity = Permission.from(request);
            permissionEntity = permissionRepository.save(permissionEntity);
            PermissionDTO.Record response = PermissionDTO.Record.from(permissionEntity);

            log.info("Permission {} registered successfully with id {}", permissionEntity.getName(), permissionEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering permission in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering permission in Tenant: {}. Trace: ", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }
}

