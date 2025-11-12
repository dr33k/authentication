package com.seven.auth.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.domain.DomainDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class ApplicationDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Create(
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        String name,
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Description must be at least 2 characters long")
        String description,
        List<DomainDTO.Create> domains
    ){
        public String schemaName(){return name;}
    }

    public record Filter(
        String name,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){ }

    public record Record(
            UUID id,
            String name,
            String description,
            ZonedDateTime dateCreated,
            ZonedDateTime dateUpdated
    ){
        public String schemaName(){return name;}
    }
}
