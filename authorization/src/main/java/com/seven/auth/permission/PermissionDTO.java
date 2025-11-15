package com.seven.auth.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class PermissionDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        String name,
        @NotNull(message = "Required field")
        PermissionType type,
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
            String description,
            @NotNull(message = "Required field")
            PermissionType type
    ){}

    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    public record Record(
            UUID id,
            String name,
            PermissionType type,
            String description,
            UUID domainId,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated,
            String createdBy,
            String updatedBy
    ) { }

    public enum PermissionType{CREATE, READ, UPDATE, DELETE}
}
