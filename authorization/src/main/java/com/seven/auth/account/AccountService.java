package com.seven.auth.account;

import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.exception.ClientException;
import com.seven.auth.exception.ConflictException;
import com.seven.auth.exception.NotFoundException;
import com.seven.auth.util.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class AccountService implements UserDetailsService, UserDetailsPasswordService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AccountService(AccountRepository accountRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
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
            log.info("Creating account: {}", accountCreateRequest.email());
            if (accountRepository.existsByEmail(accountCreateRequest.email()))
                throw new ConflictException("An account with this email already exists");

            Account account = Account.from(accountCreateRequest);

            //Encode password
            account.setPassword(bCryptPasswordEncoder.encode(accountCreateRequest.password()));
            account = accountRepository.save(account);
            AccountDTO.Record record = AccountDTO.Record.from(account);

            log.info("Account {} successfully created", account.getId());
            return record;
        } catch (AuthorizationException e) {
            log.error("Unable to create account: {}. Message: {}", accountCreateRequest.email(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unable to create account: {}. Message: ", accountCreateRequest.email(), e);
            throw new ConflictException(e.getMessage());
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