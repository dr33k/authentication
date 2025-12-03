package com.seven.auth.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.account.AccountDTO;
import com.seven.auth.domain.DomainDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class ApplicationDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "AppCreateRequest")
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^\\w{2,30}$", message = "Name must be a sequence of at least 2 alphanumeric characters")
        String name,
        @Pattern(regexp = "^\\w{2,30}$", message = "Description must be a sequence of at least 2 alphanumeric characters")
        String description,
        List<DomainDTO.Create> domains
    ){
        public String schemaName(){return name;}
    }

    @Schema(name = "AppFilterRequest")
    @Validated
    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){ }

    @Schema(name = "AppResponse")
    public record Record(
            UUID id,
            String name,
            String description,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated,
            String createdBy,
            String updatedBy
    ){
        public String schemaName(){return name;}

        public static Record from(Application app){
            return new Record(
                    app.getId(),
                    app.getName(),
                    app.getDescription(),
                    app.getDateCreated(),
                    app.getDateUpdated(),
                    app.getCreatedBy(),
                    app.getUpdatedBy()
            );
        }
    }
}
