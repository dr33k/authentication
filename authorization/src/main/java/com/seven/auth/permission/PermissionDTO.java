package com.seven.auth.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.account.AccountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class PermissionDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "PermissionCreateRequest")
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of 2-30 alphanumeric characters")
        String name,
        @NotNull(message = "Required field")
        Permission.PermissionType type,
        @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of 2-30 alphanumeric characters")
        String description,
        @NotNull(message = "Required field")
        UUID domainId
    ){}

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "PermissionUpdateRequest")
    public record Update(
            @NotBlank(message = "Required field")
            @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of 2-30 alphanumeric characters")
            String name,
            @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of 2-30 alphanumeric characters")
            String description,
            @NotNull(message = "Required field")
            Permission.PermissionType type
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
            Permission.PermissionType type,
            String description,
            UUID domainId,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated,
            AccountDTO.MinRecord createdBy,
            AccountDTO.MinRecord updatedBy
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
                    AccountDTO.MinRecord.from(p.getCreatedBy()),
                    AccountDTO.MinRecord.from(p.getUpdatedBy())
            );
        }
    }
}
