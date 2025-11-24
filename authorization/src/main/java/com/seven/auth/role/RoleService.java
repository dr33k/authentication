package com.seven.auth.role;

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
public class RoleService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Page<RoleDTO.Record> getAll(Pagination pagination, RoleDTO.Filter filter) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Rolesfor Tenant: {}", tenant);
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<RoleDTO.Record> rolesResponse = roleRepository
                    .findAll(RoleSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(RoleDTO.Record::from);

            log.info("Roles retrieved successfully");
            return rolesResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Roles for Tenant: {} in service layer. Trace:", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public RoleDTO.Record get(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Role: {} for Tenant: {}", id, tenant);
        try {
            Role roleEntity = roleRepository.findById(id).orElseThrow(() -> {
                log.error("Role: {} not found", id);
                return new NotFoundException(String.format("Role: %s not found", id));
            });

            RoleDTO.Record response = RoleDTO.Record.from(roleEntity);
            log.info("Role retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving role in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Role: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Deleting Role: {} for Tenant: {}", id, tenant);
        try {
            roleRepository.deleteById(id);
            log.info("Role deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Role {} not found.", id);
            throw new NotFoundException(String.format("Role %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Role: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public RoleDTO.Record create(RoleDTO.Create request) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Registering Role for Tenant: {}", tenant);
        try {
            //Validate unique name for an organisation
            if (roleRepository.existsByName(request.name())) {
                log.error("Role with name '{}' already exists", request.name());
                throw new ConflictException(String.format("Role with name '%s' already exists", request.name()));
            }

            Role roleEntity = Role.from(request);
            roleEntity = roleRepository.save(roleEntity);
            RoleDTO.Record response = RoleDTO.Record.from(roleEntity);

            log.info("Role {} registered successfully with id {}", roleEntity.getName(), roleEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering role in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering role in Tenant: {}. Trace: ", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }
}

