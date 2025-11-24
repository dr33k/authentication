package com.seven.auth.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RoleDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        String name,
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Description must be at least 2 characters long")
        String description
    ){}

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Update(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        String name,
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Description must be at least 2 characters long")
        String description
        ){}

    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    public record Record(
            UUID id,
            String name,
            String description,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated,
            String createdBy,
            String updatedBy
    ) {
        public static Record from(Role r){
            return new Record(
                    r.getId(),
                    r.getName(),
                    r.getDescription(),
                    r.getDateCreated(),
                    r.getDateUpdated(),
                    r.getCreatedBy(),
                    r.getUpdatedBy()
            );
        }
    }
}
