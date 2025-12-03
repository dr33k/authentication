package com.seven.auth.grant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.account.AccountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class GrantDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "GrantCreateRequest")
    public record Create(
        @NotNull
        UUID permissionId,
        @NotNull
        UUID roleId
    ){}
    @Schema(name = "GrantFilterRequest")
    public record Filter(
            UUID permissionId,
            UUID roleId,
            ZonedDateTime dateCreatedFrom,
            ZonedDateTime dateCreatedTo
    ){}

    @Schema(name = "GrantResponse")
    public record Record(
            UUID id,
            UUID permissionId,
            UUID roleId,
            String description,
            ZonedDateTime dateCreated,
            AccountDTO.MinRecord createdBy
    ) {
        public static Record from(Grant g){
            return new GrantDTO.Record(
                    g.getId(),
                    g.getPermission().getId(),
                    g.getRole().getId(),
                    g.getDescription(),
                    g.getDateCreated(),
                    AccountDTO.MinRecord.from(g.getCreatedBy())
            );
        }
    }
}
