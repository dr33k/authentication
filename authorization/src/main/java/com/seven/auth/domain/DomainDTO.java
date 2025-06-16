package com.seven.auth.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

public record DomainDTO(
        UUID id,
        String name,
        String description,
        ZonedDateTime dateCreated,
        ZonedDateTime dateUpdated
) {

}
