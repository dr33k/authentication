package com.seven.auth.assignment;

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
public class AssignmentService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AssignmentRepository assignmentRepository;
    private final ModelMapper modelMapper;

    public AssignmentService(AssignmentRepository assignmentRepository, ModelMapper modelMapper) {
        this.assignmentRepository = assignmentRepository;
        this.modelMapper = modelMapper;

        TypeMap<AssignmentDTO.Create, Assignment> assignmentToEntityTypeMap = modelMapper.createTypeMap(AssignmentDTO.Create.class, Assignment.class);
        assignmentToEntityTypeMap.addMappings(mapper -> {
            mapper.skip(Assignment::setId);
            mapper.skip(Assignment::setDateCreated);
            mapper.skip(Assignment::setDateUpdated);
        });
    }

    public Page<AssignmentDTO.Record> getAll(Pagination pagination, AssignmentDTO.Filter filter) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Assignmentsfor Tenant: {}", tenant);
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<AssignmentDTO.Record> assignmentsResponse = assignmentRepository
                    .findAll(AssignmentSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(assignmentEntity -> modelMapper.map(assignmentEntity, AssignmentDTO.Record.class));

            log.info("Assignments retrieved successfully");
            return assignmentsResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Assignments for Tenant: {} in service layer. Trace:", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public AssignmentDTO.Record get(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Assignment: {} for Tenant: {}", id, tenant);
        try {
            Assignment assignmentEntity = assignmentRepository.findById(id).orElseThrow(() -> {
                log.error("Assignment: {} not found", id);
                return new NotFoundException(String.format("Assignment: %s not found", id));
            });

            AssignmentDTO.Record response = modelMapper.map(assignmentEntity, AssignmentDTO.Record.class);
            log.info("Assignment retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving assignment in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Assignment: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Deleting Assignment: {} for Tenant: {}", id, tenant);
        try {
            assignmentRepository.deleteById(id);
            log.info("Assignment deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Assignment {} not found.", id);
            throw new NotFoundException(String.format("Assignment %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Assignment: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public AssignmentDTO.Record create(AssignmentDTO.Create request) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Registering Assignment for Tenant: {}", tenant);
        try {
            //Validate unique name for an organisation
            if (assignmentRepository.existsByName(request.name())) {
                log.error("Assignment with name '{}' already exists", request.name());
                throw new ConflictException(String.format("Assignment with name '%s' already exists", request.name()));
            }

            Assignment assignmentEntity = modelMapper.map(request, Assignment.class);
            assignmentEntity = assignmentRepository.save(assignmentEntity);
            AssignmentDTO.Record response = modelMapper.map(assignmentEntity, AssignmentDTO.Record.class);

            log.info("Assignment {} registered successfully with id {}", assignmentEntity.getName(), assignmentEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering assignment in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering assignment in Tenant: {}. Trace: ", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }
}

