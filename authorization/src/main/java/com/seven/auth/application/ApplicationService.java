package com.seven.auth.application;

import com.seven.auth.Pagination;
import com.seven.auth.config.tenant.TenantService;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
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
public class ApplicationService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationRepository applicationRepository;
    private final TenantService tenantService;
    private final ModelMapper modelMapper;

    public ApplicationService(ApplicationRepository applicationRepository, TenantService tenantService, ModelMapper modelMapper) {
        this.applicationRepository = applicationRepository;
        this.tenantService = tenantService;
        this.modelMapper = modelMapper;

        //Purpose:
        // When a request POJO is received, skip the id field when mapping into the entity. We should always generate an id or use the one gotten from the DB in the case of an update;
        // Skip the request's dateCreated field
        TypeMap<ApplicationRequest, Application> applicationToEntityTypeMap = modelMapper.createTypeMap(ApplicationRequest.class, Application.class);
        applicationToEntityTypeMap.addMappings(mapper -> {
            mapper.skip(Application::setId);
            mapper.skip(Application::setDateCreated);
            mapper.skip(Application::setDateUpdated);
        });
    }

    public Page<ApplicationDTO> getAll(Pagination pagination, ApplicationRequest.Filter filter) throws AuthorizationException {
        log.info("Retrieving Applications");
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<ApplicationDTO> applicationsResponse = applicationRepository
                    .findAll(ApplicationSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(applicationEntity -> modelMapper.map(applicationEntity, ApplicationDTO.class));

            log.info("Applications retrieved successfully");
            return applicationsResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Applications in service layer. Trace:", e);
            throw new ClientException(e.getMessage());
        }
    }

    public ApplicationDTO get(UUID id) throws AuthorizationException {
        log.info("Retrieving Application: {}", id);
        try {
            Application applicationEntity = applicationRepository.findById(id).orElseThrow(() -> {
                log.error("Application: {} not found", id);
                return new NotFoundException(String.format("Application: %s not found", id));
            });

            ApplicationDTO response = modelMapper.map(applicationEntity, ApplicationDTO.class);
            log.info("Application retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving Application: {}. Reason: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Application: {} in service layer. Trace:", id, e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        log.info("Deleting Application: {} ", id);
        try {
            applicationRepository.deleteById(id);
            log.info("Application deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Application {} not found.", id);
            throw new NotFoundException(String.format("Application %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Application: {} in service layer. Trace:", id, e);
            throw new ClientException(e.getMessage());
        }
    }

    public ApplicationDTO create(ApplicationRequest.Create request) throws AuthorizationException {
//        tenantService.register();
        return null;
    }
}

