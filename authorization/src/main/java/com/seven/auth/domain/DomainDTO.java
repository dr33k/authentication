package com.seven.auth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.permission.PermissionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class DomainDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "DomainCreateRequest")
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of at 2-30 alphanumeric characters")
        String name,
        @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of 2-30 alphanumeric characters")
        String description,
        @NotEmpty @NotNull
        List<PermissionDTO.Create> permissions
    ){}

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "DomainUpdateRequest")
    public record Update(
            @NotBlank(message = "Required field")
            @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of at 2-30 alphanumeric characters")
            String name,
            @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of 2-30 alphanumeric characters")
            String description
        ){}

    @Schema(name = "DomainFilterRequest")
    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    @Schema(name = "DomainResponse")
    public record Record(
            UUID id,
            String name,
            String description,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated,
            String createdBy,
            String updatedBy
    ) {
        public static Record from(Domain d){
            return new Record(
                    d.getId(),
                    d.getName(),
                    d.getDescription(),
                    d.getDateCreated(),
                    d.getDateUpdated(),
                    d.getCreatedBy(),
                    d.getUpdatedBy()
            );
        }
    }
}
