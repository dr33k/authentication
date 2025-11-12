package com.seven.auth.account;

import lombok.Builder;

@Builder
public class AuthDTO {
    public AccountDTO.Record data;
    public String token;
}
