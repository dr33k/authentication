package com.seven.auth.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seven.auth.domain.Domain;
import com.seven.auth.domain.DomainDTO;
import com.seven.auth.domain.DomainRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;

public class ApplicationRequest {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class Create{
        @NotBlank(message = "Required field")
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Name must be at least 2 characters long")
        private String name;
        @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Description must be at least 2 characters long")
        private String description;
        private List<DomainRequest.Create> domains;
    }

    @Data
    public static class Filter{
        private String name;
        private ZonedDateTime dateCreatedFrom;
        private ZonedDateTime dateCreatedTo;
    }
}
