package com.seven.auth.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.ZonedDateTime;
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record AccountRecord(
        String firstName,
        String lastName,
        String phoneNo,
        String email,
        @JsonIgnore
        String password,
        LocalDate dateBirth,
        ZonedDateTime dateReg,
        ZonedDateTime dateModified,
        String message
) {
    public static AccountRecord copy(Account u){
        return new AccountRecord(
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNo(),
                u.getEmail(),
                u.getPassword(),
                u.getDob(),
                u.getDateCreated(),
                u.getDateUpdated(),
                null
        );
    }
    public static AccountRecord copy(AccountRequest.Update u){
        return new AccountRecord(
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNo(),
                null,
                u.getPassword(),
                u.getDob(),
                null,
                null,
                null
        );
    }
    public static AccountRecord copy(AccountRequest.Create u){
        return new AccountRecord(
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNo(),
                u.getEmail(),
                u.getPassword(),
                u.getDob(),
                null,
                null,
                null
        );
    }
    public String toString(){
        return this.email;
    }
}