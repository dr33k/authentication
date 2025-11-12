package com.seven.auth.account;

import com.seven.auth.util.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    public AccountService(AccountRepository accountRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          Authentication authentication, ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
        this.authentication = authentication;
        this.modelMapper = modelMapper;
    }

    public Page<AccountDTO.Record> getAll(Pagination pagination, AccountDTO.Filter accountFilter) {
        try {
            log.info("Fetching accounts: limit {}, offset {}", pagination.getLimit(), pagination.getOffset());
            Pageable pageable = PageRequest.of(pagination.getLimit(), pagination.getOffset(),
                    Sort.by(
                            Sort.Direction.fromString(pagination.getSortOrder() == null ? "DESC" : pagination.getSortOrder()),
                            pagination.getSortField() == null ? "dateCreated" : pagination.getSortField()
                    ));

            return accountRepository.findAll(AccountSearchSpecification.getAllAndFilter(accountFilter), pageable).map(account -> modelMapper.map(account, AccountDTO.Record.class));
        } catch (Exception e) {
            log.error("Unable to fetch accounts. Message: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public AccountDTO.Record get(UUID id) {
        try {
            log.info("Fetching account: {}", id);
            Account accountFromDb = accountRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "This account does not exist or has been deleted"));

            AccountDTO.Record record = modelMapper.map(accountFromDb, AccountDTO.Record.class);
            log.info("Account {} successfully retrieved", id);
            return record;
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to fetch account: {}. Message: ", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to fetch account: {}. Message: ", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional
    public AccountDTO.Record create(AccountDTO.Create accountCreateRequest) {
        try {
            log.info("Creating account: {}", accountCreateRequest.email());
            if (accountRepository.existsByEmail(accountCreateRequest.email()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");

            Account account = new Account();
            BeanUtils.copyProperties(accountCreateRequest, account);

            //Encode password
            account.setPassword(bCryptPasswordEncoder.encode(accountCreateRequest.password()));
            account = accountRepository.save(account);
            log.info("Account: {} created successfully", account.getId());

            return modelMapper.map(account, AccountDTO.Record.class);
        } catch (ResponseStatusException e) {
            log.error("ResponseStatusException; Unable to create account: {}. Message: ", accountCreateRequest.email(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unable to create account: {}. Message: ", accountCreateRequest.email(), e);
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
    public AccountDTO.Record update(UUID id, AccountDTO.Update accountUpdateRequest) {
        try {
            log.info("Modifying account: {}", id);
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account account could not be found"));

            BeanUtils.copyProperties(accountUpdateRequest, account);
            accountRepository.save(account);
            log.info("Account: {} modified successfully", account.getId());

            return modelMapper.map(account, AccountDTO.Record.class);
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