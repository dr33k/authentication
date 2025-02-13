package com.seven.auth.user;

import lombok.Builder;

@Builder
public class UserDTO {
    public UserRecord data;
    public String token;
}
