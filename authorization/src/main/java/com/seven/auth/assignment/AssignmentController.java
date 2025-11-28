package com.seven.auth.assignment;

import com.seven.auth.util.Constants;
import com.seven.auth.util.Pagination;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.response.Response;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.util.response.Responder.noContent;
import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity <Response> getResources(@ParameterObject Pagination pagination, @ParameterObject AssignmentDTO.Filter assignmentFilter)  throws AuthorizationException {
        Page<AssignmentDTO.Record> assignmentRecords = assignmentService.getAll(pagination, assignmentFilter);
        return ok(assignmentRecords);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody AssignmentDTO.Create request) throws AuthorizationException {
        AssignmentDTO.Record assignmentRecord = assignmentService.create(request);
        return ok(assignmentRecord);
    }

    @DeleteMapping("{assignmentId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "assignmentId") UUID id) throws AuthorizationException {
        assignmentService.delete(id);
        return noContent();
    }
}