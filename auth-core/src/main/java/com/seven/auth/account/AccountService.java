package com.seven.auth.account;

import com.seven.auth.Pagination;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ApplicationScope
public class AccountService implements UserDetailsService {
    private AccountRepository accountRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Authentication authentication;

    public AccountService(AccountRepository accountRepository,
                          BCryptPasswordEncoder passwordEncoder ,
                          Authentication authentication) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
        this.authentication = authentication;
    }

    public List <AccountRecord> getAll(Pagination pagination) {
        Page<Account> accountList = accountRepository.findAllLimitOffset(pagination.getLimit(), pagination.getOffset());

        List <AccountRecord> accountRecords =
                accountList.stream().map(AccountRecord::copy).collect(Collectors.toList());

        return accountRecords;
    }

    public AccountRecord get(UUID id) {
        Account accountFromDb;
         //Signifies account owner access.
            accountFromDb = accountRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND ,
                            "This account does not exist or has been deleted"));

        return AccountRecord.copy(accountFromDb);
    }

    @Transactional
    public AccountRecord create(AccountRequest.Create accountCreateRequest) {
        try {
            if (accountRepository.existsByEmail(accountCreateRequest.getEmail()))
                throw new ResponseStatusException(HttpStatus.CONFLICT , "An account with this email already exists");

            Account account = new Account();
            BeanUtils.copyProperties(accountCreateRequest, account);

            //Encode password
            account.setPassword(bCryptPasswordEncoder.encode(accountCreateRequest.getPassword()));
            //Save
            accountRepository.save(account);

            return AccountRecord.copy(account);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,
                    "Account could not be created, please try again later. Why? " + ex.getMessage());
        }
    }

    @Transactional
    public void delete(UUID id) {
        Account account = (Account) authentication.getPrincipal();
        if (account.getId() != id) throw new ResponseStatusException(HttpStatus.FORBIDDEN , "Account Breach");

        accountRepository.deleteById(id);
    }

    @Transactional
    public AccountRecord update(UUID id, AccountRequest.Update accountUpdateRequest) {
        try {
            Account account = accountRepository.findById(id)
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Account account could not be found"));

            Boolean modified = false;

            //If the property is not null
            if (accountUpdateRequest.getFirstName() != null) {
                account.setFirstName(accountUpdateRequest.getFirstName());
                modified = true;
            }
            if (accountUpdateRequest.getLastName() != null) {
                account.setLastName(accountUpdateRequest.getLastName());
                modified = true;
            }
            if (accountUpdateRequest.getPassword() != null) {
                account.setPassword(accountUpdateRequest.getPassword());
                modified = true;
            }
            if (accountUpdateRequest.getPhoneNo() != null) {
                account.setPhoneNo(accountUpdateRequest.getPhoneNo());
                modified = true;
            }
            if (accountUpdateRequest.getDob() != null) {
                account.setDob(accountUpdateRequest.getDob());
                modified = true;
            }
            if (modified) accountRepository.save(account);

            return AccountRecord.copy(account);

        }catch (ResponseStatusException ex) {throw ex;}
        catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account could not be modified, Message: " + ex.getMessage());
        }
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}