package com.seven.auth.role;

import com.seven.auth.Pagination;
import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.config.tenant.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.exception.NotFoundException;
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
    private final ModelMapper modelMapper;

    public RoleService(RoleRepository roleRepository, ModelMapper modelMapper) {
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;

        //Purpose:
        // When a request POJO is recieved, skip the id field when mapping into the entity. We should always generate an id or use the one gotten from the DB in the case of an update;
        // Skip the request's dateCreated field
        TypeMap<RoleRequest, Role> roleToEntityTypeMap = modelMapper.createTypeMap(RoleRequest.class, Role.class);
        roleToEntityTypeMap.addMappings(mapper -> {
            mapper.skip(Role::setId);
            mapper.skip(Role::setDateCreated);
            mapper.skip(Role::setDateUpdated);
        });
    }

    public Page<RoleDTO> getAll(Pagination pagination, RoleRequest.Filter filter) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Retrieving Roles for Application: {}", targetApplication.id());
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<RoleDTO> rolesResponse = roleRepository
                    .findAll(RoleSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(roleEntity -> modelMapper.map(roleEntity, RoleDTO.class));

            log.info("Roles retrieved successfully");
            return rolesResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Roles for Application: {} in service layer. Trace: {}", targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public RoleDTO get(UUID id) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Retrieving Role: {} for  Application: {}", id, targetApplication.id());
        try {
            Role roleEntity = roleRepository.findById(id).orElseThrow(() -> {
                log.error("Role: {} not found", id);
                return new NotFoundException(String.format("Role: %s not found", id));
            });

            RoleDTO response = modelMapper.map(roleEntity, RoleDTO.class);
            log.info("Role retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving role in Application: {}. Reason: {}", targetApplication.id(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Role: {} for Application: {} in service layer. Trace: {}", id, targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Deleting Role: {} for  Application: {}", id, targetApplication.id());
        try {
            roleRepository.deleteById(id);
            log.info("Role deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Role {} not found.", id);
            throw new NotFoundException(String.format("Role %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Role: {} for Application: {} in service layer. Trace: {}", id, targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public RoleDTO create(RoleRequest.Create request) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Registering Roles for Application: {}", targetApplication.id());
        try {
            //Validate unique name for an organisation
            if (roleRepository.existsByName(request.getName())) {
                log.error("Role with name '{}' already exists", request.getName());
                throw new ConflictException(String.format("Role with name '%s' already exists", request.getName()));
            }

            Role roleEntity = modelMapper.map(request, Role.class);
            roleEntity = roleRepository.save(roleEntity);
            RoleDTO response = modelMapper.map(roleEntity, RoleDTO.class);

            log.info("Role {} registered successfully with id {}", roleEntity.getName(), roleEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering role in Application: {}. Reason: {}", targetApplication.id(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering role in Application: {}. Trace: ", targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }
}

