package com.seven.auth.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class AccountRequest {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Create {
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        private String firstName;
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        private String lastName;
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[+-][0-9]{10,20}$", message = "Invalid phone number format. Include country code")
        private String phoneNo;
        @NotBlank(message = "Required field")
        @Email(regexp = "[\\w{2,}@\\w{2,}\\.\\w{2,}", message = "Invalid email format")
        private String email;
        @Pattern(regexp = "^[A-Za-z\\d'.!@#$%^&*_\\-]{8,}",message = "Password must fulfill all requirements")
        private String password;
        @Past(message = "Future and current dates not allowed")
        private LocalDate dob;
    }

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public class Update {
        @Pattern(regexp = "^[A-Za-z'\\-]{2,30}",message = "Name must be at least 2 characters long")
        String firstName;
        @Pattern(regexp = "^[A-Za-z'\\-]{2,30}",message = "Name must be at least 2 characters long")
        String lastName ;
        @Pattern(regexp = "^[+-][0-9]{10,20}$",message = "Name must be at least 2 characters long")
        String phoneNo ;
        @NotBlank
        @Email(regexp = "[\\w{2,}@\\w{2,}\\.\\w{2,}", message = "Invalid email format")
        String email;
        @Pattern(regexp = "^[A-Za-z\\d'.!@#$%^&*_\\-]{8,}",message = "Password must fulfill all requirements")
        String password;
        @Past(message = "Future and current dates not allowed")
        LocalDate dob;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Filter {
        private String name;
        private String phoneNo;
        private String email;
        private LocalDate dob;
        private ZonedDateTime dateCreatedFrom;
        private ZonedDateTime dateCreatedTo;
    }
}
