package com.seven.auth.assignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.UUID;

public class AssignmentDTO {

    @Validated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "AssignmentCreateRequest")
    public record Create(
        @NotBlank(message = "Required field")
        @Email(regexp = "\\w{2,}@\\w{2,}\\.\\w{2,}", message = "Invalid email format")
        String accountEmail,
        @NotNull
        UUID roleId
    ){}

    @Schema(name = "AssignmentFilterRequest")
    public record Filter(
        String accountEmail,
        UUID roleId,
        ZonedDateTime dateCreatedFrom,
        ZonedDateTime dateCreatedTo
    ){}

    @Schema(name = "AssignmentResponse")
    public record Record(
            String accountEmail,
            UUID roleId,
            ZonedDateTime dateCreated
    ) {
        public static Record from(Assignment assignment){
            return new Record(
                    assignment.getId().getAccountEmail(),
                    assignment.getId().getRoleId(),
                    assignment.getDateCreated()
            );
        }
    }
}
