package com.seven.auth.config.autdit;

import com.seven.auth.account.Account;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.account.AccountRepository;
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
        AccountDTO.Record record = (AccountDTO.Record)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return Optional.of(Account.from(record));
    }
}
