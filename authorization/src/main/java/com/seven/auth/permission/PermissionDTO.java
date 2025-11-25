package com.seven.auth.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.grant.Grant;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "PermissionCreateRequest")
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
    @Schema(name = "PermissionUpdateRequest")
    public record Update(
            @NotBlank(message = "Required field")
            @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
            String name,
            @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Description must be at least 2 characters long")
            String description,
            @NotNull(message = "Required field")
            PermissionType type
    ){}

    @Schema(name = "PermissionFilterRequest")
    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    @Schema(name = "PermissionResponse")
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
    ) {
        public static Record from(Permission p){
            return new Record(
                    p.getId(),
                    p.getName(),
                    p.getType(),
                    p.getDescription(),
                    p.getDomain().getId(),
                    p.getDateCreated(),
                    p.getDateUpdated(),
                    p.getCreatedBy(),
                    p.getUpdatedBy()
            );
        }
    }

    public enum PermissionType{CREATE, READ, UPDATE, DELETE}
}
