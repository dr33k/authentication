package com.seven.auth.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.account.AccountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RoleDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "RoleCreateRequest")
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of 2-30 alphanumeric characters")
        String name,
        @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of 2-30 alphanumeric characters")
        String description
    ){}

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "RoleUpdateRequest")
    public record Update(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of 2-30 alphanumeric characters")
        String name,
        @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of 2-30 alphanumeric characters")
        String description
        ){}

    @Schema(name = "RoleFilterRequest")
    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    @Schema(name = "RoleResponse")
    public record Record(
            UUID id,
            String name,
            String description,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated,
            AccountDTO.MinRecord createdBy,
            AccountDTO.MinRecord updatedBy
    ) {
        public static Record from(Role r){
            return new Record(
                    r.getId(),
                    r.getName(),
                    r.getDescription(),
                    r.getDateCreated(),
                    r.getDateUpdated(),
                    AccountDTO.MinRecord.from(r.getCreatedBy()),
                    AccountDTO.MinRecord.from(r.getUpdatedBy())
            );
        }
    }
}
