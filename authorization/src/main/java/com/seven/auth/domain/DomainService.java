package com.seven.auth.domain;

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
public class DomainService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final DomainRepository domainRepository;
    private final ModelMapper modelMapper;

    public DomainService(DomainRepository domainRepository, ModelMapper modelMapper) {
        this.domainRepository = domainRepository;
        this.modelMapper = modelMapper;

        //Purpose:
        // When a request POJO is recieved, skip the id field when mapping into the entity. We should always generate an id or use the one gotten from the DB in the case of an update;
        // Skip the request's dateCreated field
        TypeMap<DomainRequest, Domain> domainToEntityTypeMap = modelMapper.createTypeMap(DomainRequest.class, Domain.class);
        domainToEntityTypeMap.addMappings(mapper -> {
            mapper.skip(Domain::setId);
            mapper.skip(Domain::setDateCreated);
            mapper.skip(Domain::setDateUpdated);
        });
    }

    public Page<DomainDTO> getAll(Pagination pagination, DomainRequest.Filter filter) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Retrieving Domains for Application: {}", targetApplication.id());
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<DomainDTO> domainsResponse = domainRepository
                    .findAll(DomainSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(domainEntity -> modelMapper.map(domainEntity, DomainDTO.class));

            log.info("Domains retrieved successfully");
            return domainsResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Domains for Application: {} in service layer. Trace: {}", targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public DomainDTO get(UUID id) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Retrieving Domain: {} for  Application: {}", id, targetApplication.id());
        try {
            Domain domainEntity = domainRepository.findById(id).orElseThrow(() -> {
                log.error("Domain: {} not found", id);
                return new NotFoundException(String.format("Domain: %s not found", id));
            });

            DomainDTO response = modelMapper.map(domainEntity, DomainDTO.class);
            log.info("Domain retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving domain in Application: {}. Reason: {}", targetApplication.id(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Domain: {} for Application: {} in service layer. Trace: {}", id, targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Deleting Domain: {} for  Application: {}", id, targetApplication.id());
        try {
            domainRepository.deleteById(id);
            log.info("Domain deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Domain {} not found.", id);
            throw new NotFoundException(String.format("Domain %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Domain: {} for Application: {} in service layer. Trace: {}", id, targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }

    public DomainDTO create(DomainRequest.Create request) throws AuthorizationException {
        ApplicationDTO targetApplication = TenantContext.getCurrentTenant();
        log.info("Registering Domains for Application: {}", targetApplication.id());
        try {
            //Validate unique name for an organisation
            if (domainRepository.existsByName(request.getName())) {
                log.error("Domain with name '{}' already exists", request.getName());
                throw new ConflictException(String.format("Domain with name '%s' already exists", request.getName()));
            }

            Domain domainEntity = modelMapper.map(request, Domain.class);
            domainEntity = domainRepository.save(domainEntity);
            DomainDTO response = modelMapper.map(domainEntity, DomainDTO.class);

            log.info("Domain {} registered successfully with id {}", domainEntity.getName(), domainEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering domain in Application: {}. Reason: {}", targetApplication.id(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering domain in Application: {}. Trace: ", targetApplication.id(), e);
            throw new ClientException(e.getMessage());
        }
    }
}

