package com.seven.auth.config.audit;

import com.seven.auth.account.Account;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AccountRepository;
import com.seven.auth.config.threadlocal.TenantContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountAuditorAware implements AuditorAware<Account> {
    private final AccountRepository accountRepository;

    public AccountAuditorAware(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> getCurrentAuditor() {
        if(TenantContext.getAuditor() == null) { //If no auditor was manually set, let the auditor be the current authenticated principal
            AccountDTO.Record record = (AccountDTO.Record) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return Optional.of(Account.from(record));
        } else{
            return Optional.of(TenantContext.getAuditor());
        }
    }
}
