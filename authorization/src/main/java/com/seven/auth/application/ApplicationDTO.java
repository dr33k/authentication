package com.seven.auth.application;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ApplicationDTO(
        UUID id,
        String name,
        @JsonIgnore String schemaName,
        String description,
        ZonedDateTime dateCreated,
        ZonedDateTime dateUpdated
) {

}
