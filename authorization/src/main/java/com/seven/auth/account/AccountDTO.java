package com.seven.auth.account;

import lombok.Builder;

@Builder
public class AccountDTO {
    public AccountRecord data;
    public String token;
}
