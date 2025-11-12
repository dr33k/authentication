package com.seven.auth.domain;

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
public class DomainService{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final DomainRepository domainRepository;
    private final ModelMapper modelMapper;

    public DomainService(DomainRepository domainRepository, ModelMapper modelMapper) {
        this.domainRepository = domainRepository;
        this.modelMapper = modelMapper;

        TypeMap<DomainDTO, Domain> domainToEntityTypeMap = modelMapper.createTypeMap(DomainDTO.class, Domain.class);
        domainToEntityTypeMap.addMappings(mapper -> {
            mapper.skip(Domain::setId);
            mapper.skip(Domain::setDateCreated);
            mapper.skip(Domain::setDateUpdated);
        });
    }

    public Page<DomainDTO.Record> getAll(Pagination pagination, DomainDTO.Filter filter) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Domainsfor Tenant: {}", tenant);
        try {
            Pageable pageable = PageRequest.of(pagination.getOffset(), pagination.getLimit(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    )
            );
            Page<DomainDTO.Record> domainsResponse = domainRepository
                    .findAll(DomainSearchSpecification.getAllAndFilter(filter), pageable)
                    .map(domainEntity -> modelMapper.map(domainEntity, DomainDTO.Record.class));

            log.info("Domains retrieved successfully");
            return domainsResponse;
        } catch (Exception e) {
            log.error("Exception retrieving Domains for Tenant: {} in service layer. Trace:", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public DomainDTO.Record get(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Retrieving Domain: {} for Tenant: {}", id, tenant);
        try {
            Domain domainEntity = domainRepository.findById(id).orElseThrow(() -> {
                log.error("Domain: {} not found", id);
                return new NotFoundException(String.format("Domain: %s not found", id));
            });

            DomainDTO.Record response = modelMapper.map(domainEntity, DomainDTO.Record.class);
            log.info("Domain retrieved successfully");
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException retrieving domain in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception retrieving Domain: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Deleting Domain: {} for Tenant: {}", id, tenant);
        try {
            domainRepository.deleteById(id);
            log.info("Domain deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            log.error("Domain {} not found.", id);
            throw new NotFoundException(String.format("Domain %s not found", id));
        } catch (Exception e) {
            log.error("Exception deleting Domain: {} for Tenant: {} in service layer. Trace:", id, tenant, e);
            throw new ClientException(e.getMessage());
        }
    }

    public DomainDTO.Record create(DomainDTO.Create request) throws AuthorizationException {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Registering Domain for Tenant: {}", tenant);
        try {
            //Validate unique name for an organisation
            if (domainRepository.existsByName(request.name())) {
                log.error("Domain with name '{}' already exists", request.name());
                throw new ConflictException(String.format("Domain with name '%s' already exists", request.name()));
            }

            Domain domainEntity = modelMapper.map(request, Domain.class);
            domainEntity = domainRepository.save(domainEntity);
            DomainDTO.Record response = modelMapper.map(domainEntity, DomainDTO.Record.class);

            log.info("Domain {} registered successfully with id {}", domainEntity.getName(), domainEntity.getId());
            return response;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException registering domain in Tenant: {}. Reason: {}", tenant, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering domain in Tenant: {}. Trace: ", tenant, e);
            throw new ClientException(e.getMessage());
        }
    }
}

