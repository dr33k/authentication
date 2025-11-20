package com.seven.auth.assignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class AssignmentDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Create(
        @NotBlank(message = "Required field")
        @Email(regexp = "[\\w{2,}@\\w{2,}\\.\\w{2,}", message = "Invalid email format")
        String accountEmail,
        @NotNull
        UUID roleId
    ){}

    public record Filter(
        String accountEmail,
        UUID roleId,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    public record Record(
            String accountEmail,
            UUID roleId,
            ZonedDateTime dateCreated
    ) {
        public static Record from(Assignment assignment){
            return new Record(
                    assignment.getId().getAccountEmail(),
                    assignment.getId().getRoleId(),
                    assignment.getDateCreated()
            );
        }
    }
}
