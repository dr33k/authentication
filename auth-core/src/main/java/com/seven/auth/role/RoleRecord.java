package com.seven.auth.role;

import java.time.ZonedDateTime;
import java.util.UUID;

public record RoleRecord(
        UUID id,
        String name,
        String description,
        ZonedDateTime dateCreated,
        ZonedDateTime dateUpdated
) {

}
