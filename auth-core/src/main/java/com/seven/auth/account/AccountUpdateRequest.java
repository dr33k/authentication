package com.seven.auth.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AccountUpdateRequest {
    @Pattern(regexp = "^[A-Za-z'\\-]{2,30}",message = "Name must be at least 2 characters long")
    String firstName;
    @Pattern(regexp = "^[A-Za-z'\\-]{2,30}",message = "Name must be at least 2 characters long")
    String lastName ;
    @Pattern(regexp = "^[+-][0-9]{11,15}$",message = "Name must be at least 2 characters long")
    String phoneNo ;
    @NotBlank
    @Email(regexp = "[A-Za-z0-9\\-]{2,}@[A-Za-z'\\-]{2,}\\.[A-Za-z'\\-]{2,}", message = "Invalid email format")
    String email;
    @Pattern(regexp = "^[A-Za-z\\d'.!@#$%^&*_\\-]{8,}",message = "Password must be at least 8 characters long")
    String password;
    @Past(message = "Future and current dates not allowed")
    LocalDate dateBirth;
    UserRole role;
    Boolean isAccountNonExpired;
    Boolean isAccountNonLocked;
    Boolean isCredentialsNonExpired;
    Boolean isEnabled;
}
