package com.seven.auth.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.ZonedDateTime;
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record UserRecord(
        String firstName,
        String lastName,
        String phoneNo,
        String email,
        @JsonIgnore
        String password,
        LocalDate dateBirth,
        ZonedDateTime dateReg,
        ZonedDateTime dateModified,
        UserRole role,
        Boolean isAccountNonExpired,
        Boolean isAccountNonLocked,
        Boolean isCredentialsNonExpired,
        Boolean isEnabled,
        String message
) {
    public static UserRecord copy(User u){
        return new UserRecord(
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNo(),
                u.getEmail(),
                u.getPassword(),
                u.getDob(),
                u.getDateCreated(),
                u.getDateUpdated(),
                u.getRole(),
                u.isAccountNonExpired(),
                u.isAccountNonLocked(),
                u.isCredentialsNonExpired(),
                u.isEnabled(),
                null
        );
    }
    public static UserRecord copy(UserUpdateRequest u){
        return new UserRecord(
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNo(),
                null,
                u.getPassword(),
                u.getDateBirth(),
                null,
                null,
                null,
                u.getIsAccountNonExpired(),
                u.getIsAccountNonLocked(),
                u.getIsCredentialsNonExpired(),
                u.getIsEnabled(),
                null
        );
    }
    public static UserRecord copy(UserCreateRequest u){
        return new UserRecord(
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNo(),
                u.getEmail(),
                u.getPassword(),
                u.getDateBirth(),
                null,
                null,
                null,
                true,
                true,
                true,
                true,
                null
        );
    }
    public String toString(){
        return this.email;
    }
}