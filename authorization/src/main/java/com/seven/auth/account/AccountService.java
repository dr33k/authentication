package com.seven.auth.account;

import com.seven.auth.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Slf4j
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Authentication authentication;

    public AccountService(AccountRepository accountRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          Authentication authentication) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
        this.authentication = authentication;
    }

    public Page<AccountRecord> getAll(Pagination pagination) {
        try {
            log.info("Fetching accounts: limit {}, offset {}", pagination.getLimit(), pagination.getOffset());
            Pageable pageable = PageRequest.of(pagination.getLimit(), pagination.getOffset(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    ));

            return accountRepository.findAll(pageable).map(AccountRecord::copy);
        } catch (Exception e) {
            log.error("Unable to fetch accounts. Message: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public AccountRecord get(UUID id) {
        try {
            log.info("Fetching account: {}", id);
            Account accountFromDb = accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "This account does not exist or has been deleted"));

            return AccountRecord.copy(accountFromDb);
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to fetch account: {}. Message: ", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to fetch account: {}. Message: ", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional
    public AccountRecord create(AccountRequest.Create accountCreateRequest) {
        try {
            log.info("Creating account: {}", accountCreateRequest.getEmail());
            if (accountRepository.existsByEmail(accountCreateRequest.getEmail()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");

            Account account = new Account();
            BeanUtils.copyProperties(accountCreateRequest, Account.class);

            //Encode password
            account.setPassword(bCryptPasswordEncoder.encode(accountCreateRequest.getPassword()));
            account = accountRepository.save(account);
            log.info("Account: {} created successfully", account.getId());

            return AccountRecord.copy(account);
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to create account: {}. Message: ", accountCreateRequest.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to create account: {}. Message: ", accountCreateRequest.getEmail(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional
    public void delete(UUID id) {
        try {
            log.info("Deleting account: {}", id);
            Account account = (Account) authentication.getPrincipal();
            if (account.getId() != id) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account Breach");

            accountRepository.deleteById(id);
            log.info("Account: {} deleted successfully", account.getId());
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to delete account: {}. Message: ", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to delete account: {}. Message: ", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional
    public AccountRecord update(UUID id, AccountRequest.Update accountUpdateRequest) {
        try {
            log.info("Modifying account: {}", id);
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account account could not be found"));

            BeanUtils.copyProperties(accountUpdateRequest, account);
            accountRepository.save(account);
            log.info("Account: {} modified successfully", account.getId());

            return AccountRecord.copy(account);
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to modify account: {}. Message: ", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to modify account: {}. Message: ", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}