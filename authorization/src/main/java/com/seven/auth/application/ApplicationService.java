package com.seven.auth.application;

import com.seven.auth.util.Pagination;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.NotFoundException;
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

    public ApplicationService(ApplicationRepository applicationRepository, TenantService tenantService) {
        this.applicationRepository = applicationRepository;
        this.tenantService = tenantService;
    }

    public Page<ApplicationDTO.Record> getAll(Pagination pagination, ApplicationDTO.Filter filter) throws AuthorizationException {
        log.info("Retrieving Applications");
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<ApplicationDTO.Record> appRecords = applicationRepository
                    .findAll(ApplicationSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(ApplicationDTO.Record::from);

            log.info("Applications retrieved successfully");
            return appRecords;
        } catch (Exception e) {
            log.error("Exception retrieving Applications in service layer. Trace:", e);
            throw new ClientException(e.getMessage());
        }
    }

    public ApplicationDTO.Record get(UUID id) throws AuthorizationException {
        log.info("Retrieving Application: {}", id);
        try {
            Application applicationEntity = applicationRepository.findById(id).orElseThrow(() -> {
                log.error("Application: {} not found", id);
                return new NotFoundException(String.format("Application: %s not found", id));
            });

            ApplicationDTO.Record record = ApplicationDTO.Record.from(applicationEntity);
            log.info("Application retrieved successfully");
            return record;
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
            Application applicationEntity = applicationRepository.findById(id).orElseThrow(() -> {
                log.error("Application: {} not found", id);
                return new NotFoundException(String.format("Application: %s not found", id));
            });
            tenantService.dropSchema(applicationEntity);
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

    public ApplicationDTO.Record create(ApplicationDTO.Create create) throws AuthorizationException {
        return tenantService.register(create);
    }
}

