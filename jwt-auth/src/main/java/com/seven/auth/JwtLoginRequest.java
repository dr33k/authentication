package com.seven.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtLoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public JwtLoginRequest() {
    }

    public @NotBlank String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank String username) {
        this.username = username;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }
}
