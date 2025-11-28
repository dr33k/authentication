package com.seven.auth.application;

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
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationService applicationService;
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "applicationId") UUID id)  throws AuthorizationException {
        ApplicationDTO.Record record = applicationService.get(id);
        return ok(record);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(@ParameterObject Pagination pagination, @ParameterObject ApplicationDTO.Filter applicationFilter)  throws AuthorizationException {
        Page<ApplicationDTO.Record> applicationDTOs = applicationService.getAll(pagination, applicationFilter);
        return ok(applicationDTOs);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody ApplicationDTO.Create create) throws AuthorizationException {
        ApplicationDTO.Record record = applicationService.create(create);
        return ok(record);
    }
    
    @DeleteMapping("{applicationId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "applicationId") UUID id) throws AuthorizationException {
        applicationService.delete(id);
        return noContent();
    }
}