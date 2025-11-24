package com.seven.auth.grant;

import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.exception.NotFoundException;
import com.seven.auth.util.Pagination;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
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
public class GrantService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GrantRepository grantRepository;

    public GrantService(GrantRepository grantRepository) {
        this.grantRepository = grantRepository;
    }

    public Page<GrantDTO.Record> getAll(Pagination pagination, GrantDTO.Filter filter) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Grantsfor Tenant: {}", tenant);
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<GrantDTO.Record> grantsResponse = grantRepository
                    .findAll(GrantSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(GrantDTO.Record::from);

            log.info("Grants retrieved successfully");
            return grantsResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Grants for Tenant: {} in service layer. Trace:", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public GrantDTO.Record get(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Grant: {} for Tenant: {}", id, tenant);
        try {
            Grant grantEntity = grantRepository.findById(id).orElseThrow(() -> {
                log.error("Grant: {} not found", id);
                return new NotFoundException(String.format("Grant: %s not found", id));
            });

            GrantDTO.Record response =GrantDTO.Record.from(grantEntity);
            log.info("Grant retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving grant in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Grant: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Deleting Grant: {} for Tenant: {}", id, tenant);
        try {
            grantRepository.deleteById(id);
            log.info("Grant deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Grant {} not found.", id);
            throw new NotFoundException(String.format("Grant %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Grant: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public GrantDTO.Record create(GrantDTO.Create request) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Registering Grant for Tenant: {}", tenant);
        try {
            //Validate unique grants
            if (grantRepository.existsByPermissionIdAndRoleId(request.permissionId(), request.roleId())) {
                String message = String.format("Permission %s has already been granted to role: %s", request.permissionId(), request.roleId());
                log.error(message);
                throw new ConflictException(message);
            }

            Grant grantEntity = Grant.from(request);
            grantEntity = grantRepository.save(grantEntity);
            GrantDTO.Record response = GrantDTO.Record.from(grantEntity);

            log.info("Grant {} registered successfully", grantEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering grant in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering grant in Tenant: {}. Trace: ", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }
}

