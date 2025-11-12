package com.seven.auth.account;

import lombok.Builder;

@Builder
public class AuthDTO {
    public AccountDTO.Response data;
    public String token;
}
