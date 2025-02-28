package com.seven.auth.account;

import org.springframework.beans.BeanUtils;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@ApplicationScope
public class AccountService implements UserDetailsService {
    private AccountRepository accountRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Authentication userAuthentication;

    public AccountService(AccountRepository accountRepository,
                          BCryptPasswordEncoder passwordEncoder ,
                          Authentication userAuthentication) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
        this.userAuthentication = userAuthentication;
    }

    //For Admin
    public Set <AccountRecord> getAll() {
        List <Account> accountList = accountRepository.findAll();

        Set <AccountRecord> userRecords =
                accountList.stream().map(AccountRecord::copy).collect(Collectors.toSet());

        return userRecords;
    }

    public AccountRecord get(UUID id) {
        Account accountFromDb;
         //Signifies account owner access.
            accountFromDb = accountRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND ,
                            "This user does not exist or has been deleted"));

        return AccountRecord.copy(accountFromDb);
    }

    public AccountRecord create(AccountCreateRequest accountCreateRequest) {
        try {
            if (accountRepository.existsByEmail(accountCreateRequest.getEmail()))
                throw new ResponseStatusException(HttpStatus.CONFLICT , "A user with this email already exists");

            Account account = new Account();
            BeanUtils.copyProperties(accountCreateRequest, account);

            //Set role
            account.setRole(AccountRole.PASSENGER);

            //Encode password
            account.setPassword(bCryptPasswordEncoder.encode(accountCreateRequest.getPassword()));
            //Save
            accountRepository.save(account);

            return AccountRecord.copy(account);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR ,
                    "Account could not be created, please try again later. Why? " + ex.getMessage());
        }
    }

    //For Account
    public void delete(UUID id) {//Only the user can deactivate their account
        Account account = (Account) userAuthentication.getPrincipal();
        if (account.getId() != id) throw new ResponseStatusException(HttpStatus.FORBIDDEN , "Account Breach");

        accountRepository.deleteById(id);
    }

    //For Account
    public AccountRecord update(UUID id, AccountUpdateRequest accountUpdateRequest) {
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
            if (accountUpdateRequest.getDateBirth() != null) {
                account.setDob(accountUpdateRequest.getDateBirth());
                modified = true;
            }
            if (modified) accountRepository.save(account);

            return AccountRecord.copy(account);

        }catch (ResponseStatusException ex) {throw ex;}
        catch (Exception ex) {
            throw new RuntimeException("Account could not be modified, please contact System Administrator. Why? " + ex.getMessage());
        }
    }

    public UserDetails loadAccountByAccountname(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}