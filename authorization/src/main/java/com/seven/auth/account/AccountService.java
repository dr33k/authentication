package com.seven.auth.account;

import com.seven.auth.application.Application;
import com.seven.auth.application.ApplicationRepository;
import com.seven.auth.config.threadlocal.TenantContext;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.exception.NotFoundException;
import com.seven.auth.util.Constants;
import com.seven.auth.util.Pagination;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountService implements UserDetailsService, UserDetailsPasswordService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EntityManager em;
    private final ApplicationRepository applicationRepository;

    public AccountService(AccountRepository accountRepository,
                          BCryptPasswordEncoder passwordEncoder, EntityManager em, ApplicationRepository applicationRepository) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
        this.em = em;
        this.applicationRepository = applicationRepository;
    }

    public Page<AccountDTO.Record> getAll(Pagination pagination, AccountDTO.Filter accountFilter) throws AuthorizationException  {
        try {
            log.info("Fetching accounts: limit {}, offset {}", pagination.getLimit(), pagination.getOffset());
            Pageable pageable = PageRequest.of(pagination.getLimit(), pagination.getOffset(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    ));

            return accountRepository.findAll(AccountSearchSpecification.getAllAndFilter(accountFilter), pageable).map(AccountDTO.Record::from);
        } catch (Exception e) {
            log.error("Unable to fetch accounts. Message: ", e);
            throw new ClientException(e.getMessage());
        }
    }

    public AccountDTO.Record get(UUID id)throws AuthorizationException {
        try {
            log.info("Fetching account: {}", id);
            Account accountFromDb = accountRepository.findById(id).orElseThrow(
                    () -> new NotFoundException("This account does not exist or has been deleted"));

            AccountDTO.Record record = AccountDTO.Record.from(accountFromDb);
            log.info("Account {} successfully retrieved", id);
            return record;
        } catch (AuthorizationException e) {
            log.error("Unable to fetch account: {}. Message: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unable to fetch account: {}. Message: ", id, e);
            throw new ClientException(e.getMessage());
        }
    }

    @Transactional
    public AccountDTO.Record create(AccountDTO.Create accountCreateRequest) throws AuthorizationException {
        try {
            log.info("Creating account: {} in schema: {}", accountCreateRequest.email(), TenantContext.getCurrentTenant());
            if (accountRepository.existsByEmail(accountCreateRequest.email()))
                throw new ConflictException("An account with this email already exists");

            Account account = Account.from(accountCreateRequest);

            //Encode password
            account.setPassword(bCryptPasswordEncoder.encode(accountCreateRequest.password()));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication == null ? accountCreateRequest.email(): ((AccountDTO.Record)authentication.getPrincipal()).email();
            account.setCreatedBy(email);
            account.setUpdatedBy(email);

            account = accountRepository.save(account);
            AccountDTO.Record record = AccountDTO.Record.from(account);

            log.info("Account {} successfully created ins schema: {}", account.getId(), TenantContext.getCurrentTenant());
            return record;
        } catch (AuthorizationException e) {
            log.error("Unable to create account: {}. Message: {}", accountCreateRequest.email(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unable to create account: {}. Message: ", accountCreateRequest.email(), e);
            throw new ConflictException(e.getMessage());
        }
    }

    @Transactional
    public AccountDTO.Record createSuper(AccountDTO.Create accountCreateRequest) throws AuthorizationException {
        try {
            log.info("Creating Superuser: {} in schema: {}", accountCreateRequest.email(), TenantContext.getCurrentTenant());
            //Persist in public schema
            AccountDTO.Record accountRecord = create(accountCreateRequest);

            //Get authenticated principal email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String principalEmail = authentication == null ? accountCreateRequest.email(): ((AccountDTO.Record)authentication.getPrincipal()).email();

            //Get all registered schemas/tenants
            Set<String> tenants = applicationRepository.findAll().stream().map(Application::getSchemaName).collect(Collectors.toSet());
            tenants.remove(Constants.PUBLIC_SCHEMA);

            Account account = Account.from(accountCreateRequest);
            for(String tenant: tenants){
                em.createNativeQuery("SET SCHEMA '%s';".formatted(tenant)).executeUpdate();
                if(!accountRepository.existsByEmail(accountCreateRequest.email())){
                    //These accounts are not meant for login functionality, they are shadow accounts
                    //BCrypt is always used for logins and no encoded digest can ever return a single underscore '_'
                    account.setPassword("_");
                    account.setCreatedBy(principalEmail);
                    account.setUpdatedBy(principalEmail);

                    account = accountRepository.save(account);
                }
            }

            log.info("Tenant schemas populated with new superuser: {}", accountCreateRequest.email());
            return accountRecord;
        } catch (Exception e) {
            log.error("Unable to create accounts. Message: ", e);
            throw new ConflictException(e.getMessage());
        } finally {
            em.createNativeQuery("SET SCHEMA '%s';".formatted(Constants.PUBLIC_SCHEMA)).executeUpdate();
        }
    }

    public void delete(UUID id) throws AuthorizationException {
        try {
            log.info("Deleting account: {}", id);
            accountRepository.deleteById(id);
            log.info("Account: {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Unable to delete account: {}. Message: {}", id, e.getMessage());
            throw new ConflictException(e.getMessage());
        }
    }

    @Transactional
    public AccountDTO.Record update(UUID id, AccountDTO.Update accountUpdateRequest) throws AuthorizationException{
        try {
            log.info("Modifying account: {}", id);
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Account account could not be found"));

            Account.update(account, accountUpdateRequest);
            accountRepository.save(account);
            AccountDTO.Record record = AccountDTO.Record.from(account);

            log.info("Account {} successfully modified", account.getId());
            return record;
        } catch (AuthorizationException e) {
            log.error("AuthorizationException; Unable to modify account: {}. Message: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unable to modify account: {}. Message: ", id, e);
            throw new ConflictException(e.getMessage());
        }
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    @Override
    public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
        Account account = (Account) userDetails;
        account.setPassword(bCryptPasswordEncoder.encode(newPassword));
        account = accountRepository.save(account);
        return account;
    }
}